import cpsc474.*;

import java.io.*;
import java.util.*;

public class CFRThrowing {
    static CribbageGame game = new CribbageGame();
    static HashMap<String, Integer> rankToVal = new HashMap<>();
    static HashMap<String, PegNode> peggingNodes = new HashMap<>();
    static HashMap<String, ThrowNode> nodes = new HashMap<>();

    static int[][] combs = {{0, 1}, {0, 2}, {0, 3}, {0, 4}, {0, 5}, {1, 2}, {1, 3}, {1, 4}, {1, 5}, {2, 3}, {2, 4}, {2, 5}, {3, 4}, {3, 5}, {4, 5}};
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        populateRanks();
        NodeLoader nodeLoader = new NodeLoader();

        // possible to start training using existing nodes and use an existing peggingpolicy instead of just greedy
        // nodes = nodeLoader.getThrowNodes("thrownodes_v1_1000k.txt");
        peggingNodes = nodeLoader.getPegNodes("pegnodes_v1_3000k.txt");
        int starting = 0;

        System.out.println("Starting CFR");
        for (int i = 0; i < 500 * 1000 + 1; i++){
            cfr(1.0, 1.0, new CFRPeggingPolicy(new GreedyPegger(), peggingNodes, true), 0, null, null, null, false, null);

            if (i%1000==0) {
                System.out.println("***");
                System.out.println(i);
                System.out.println(nodes.size());
            }
            if (i > 0 && i % 250000 == 0) {
                int k = (i) / 1000 + starting;
                nodeLoader.saveNodes("thrownodes_v2_" + k + "k.txt", nodes);
            }
        }

    }

    public static void populateRanks() {
        // construct mapping rank to value
        rankToVal.put("A", 0);
        for (int i = 2; i <= 9; i++) {
            rankToVal.put(Integer.toString(i), i-1);
        }
        // not the point values - just mapping to make sure we can sort correctly and reproducible
        rankToVal.put("T", 9);
        rankToVal.put("J", 10);
        rankToVal.put("Q", 11);
        rankToVal.put("K", 12);
    }


    /**
     * CFR for throwing strategy.
     * @param p0
     * @param p1
     * @param pegPolicy
     * @param player
     * @param keep0
     * @param throw0
     * @param hand1
     * @param secondToAct
     * @param turn
     * @return
     */
    public static double cfr(
            double p0,
            double p1,
            PegPolicy pegPolicy,
            int player,
            CribbageHand keep0,
            CribbageHand throw0,
            CribbageHand hand1,
            boolean secondToAct,
            CribbageCard turn
    ) {
        String infoSet = "";
        CribbageHand myCards = null;
        if (!secondToAct) {
            CribbageHand[] cardsInPlay = game.deal();
            turn = cardsInPlay[2].iterator().next();
            myCards = cardsInPlay[0];
            hand1 = cardsInPlay[1];
            Collections.sort(myCards.cards, new SortCards(getFlushSuit(myCards.cards)));
            Collections.sort(hand1.cards, new SortCards(getFlushSuit(hand1.cards)));
            infoSet = getInfoSet(myCards.cards, true);
        }
        else {
            infoSet = getInfoSet(hand1.cards, false);
        }


        ThrowNode node;
        if (!nodes.containsKey(infoSet)) {
            // create a new infoSet
            node = new ThrowNode();
            nodes.put(infoSet, node);
        }
        else {
            node = nodes.get(infoSet);
        }


        // for every action I can choose
        // split the card accordingly and run CFR
        // then on the next turn, do the same for opponent
        // in the opponent for loop, play out the game and evaluate
        // then switch sides
        double nodeUtil = 0.0;
        double param = player == 0 ? p0 : p1;
        float[] strategy = node.getStrategy(param);
        double[] util = new double[15];
        for (int i = 0; i < 15; i++) {
            util[i] = 0.0;
        }
        for (int i = 0; i < combs.length; i++) {

            if (!secondToAct) {
                // split the card accordingly and run cfr_pkg.CFR
                CribbageCard throw1 = myCards.cards.get(combs[i][0]);
                CribbageCard throw2 = myCards.cards.get(combs[i][1]);
                CribbageHand throwCards = new CribbageHand(List.of(new CribbageCard[]{throw1, throw2}));
                CribbageHand keptCards = myCards.remove(throw1);
                keptCards = keptCards.remove(throw2);

                util[i] = -cfr(p0 * strategy[i], p1, pegPolicy, 1 - player, keptCards, throwCards, hand1, true, turn);

                nodeUtil += strategy[i] * util[i];
            }
            else {
                CribbageCard throw1 = hand1.cards.get(combs[i][0]);
                CribbageCard throw2 = hand1.cards.get(combs[i][1]);
                CribbageHand throwCards = new CribbageHand(List.of(new CribbageCard[]{throw1, throw2}));
                CribbageHand keptCards = hand1.remove(throw1);
                keptCards = keptCards.remove(throw2);

                util[i] = -play(pegPolicy, keep0, keptCards, throw0, throwCards, turn);

                // negative because this is for second player
                nodeUtil += strategy[i] * util[i];
            }

        }

        for (int i = 0; i < combs.length; i++) {
            double regret = util[i] - nodeUtil;
            node.regretSum[i] += regret * (player == 0 ? p1 : p0);
        }

        return nodeUtil;
    }

    private static String getFlushSuit(List<CribbageCard> cards) {
        return null; // for training non flush method


        // uncomment below to train suit-conscious method
        /*HashMap<String, Integer> suitCounts = new HashMap<>();
        for (cpsc474.CribbageCard c : cards) {
            String suit = String.valueOf(c.getSuit());
            if (!suitCounts.containsKey(suit)) {
                suitCounts.put(suit, 0);
            }
            suitCounts.put(suit, suitCounts.get(suit) + 1);
        }


        String flushSuit = null; // appears at least 4 times
        for (String suit : suitCounts.keySet()) {
            if (suitCounts.get(suit) >= 4) {
                flushSuit = suit;
                break;
            }
        }
        return flushSuit;*/
    }

    public static int play(PegPolicy peggingPolicy, CribbageHand keep0, CribbageHand keep1, CribbageHand throw0, CribbageHand throw1, CribbageCard turn)
    {
        int[] scores = new int[] {0, 0};
        int dealer = 0;

        // check for 2 for heels (turned card is a Jack)
        int heels = game.turnCardValue(turn);
        scores[dealer] += heels;

        // initialize pegging
        int pegTurn = 1 - dealer;
        PeggingHistory history = new PeggingHistory(game);
        CribbageHand[] pegCards = new CribbageHand[] {keep0, keep1};
        while (!history.isTerminal())
        {
            // get player's played card
            CribbageCard play = peggingPolicy.peg(pegCards[pegTurn], history, pegTurn == 0 ? Arrays.copyOf(scores, scores.length) : MoreArrays.reverse(scores), pegTurn == dealer);

            // check for legality of chosen card
            if (play == null && history.hasLegalPlay(pegCards[pegTurn], pegTurn == dealer ? 0 : 1))
            {
                throw new RuntimeException("passing when " + pegCards[pegTurn] + " contains a valid card");
            }
            else if (play != null && !history.isLegal(play, pegTurn == dealer ? 0 : 1))
            {
                throw new RuntimeException("chosen card " + play + " us not legal");
            }

            history = history.play(play, pegTurn == dealer ? 0 : 1);

            // score the play
            int[] playScore = history.getScore();
            if (playScore[0] > 0)
            {
                scores[pegTurn] += playScore[0];
            }
            else if (playScore[0] < 0)
            {
                scores[1 - pegTurn] += -playScore[0];
            }

            // remove played card from hand
            if (play != null)
            {
                CribbageHand newHand = pegCards[pegTurn].remove(play);
                if (newHand == null)
                {
                    throw new RuntimeException("played card " + play + " not in hand " + pegCards[pegTurn]);
                }
                pegCards[pegTurn] = newHand;
            }

            // next player's turn
            pegTurn = 1 - pegTurn;
        }

        // score non-dealer's hand
        int[] handScore = game.score(keep1, turn, false);
        scores[1 - dealer] += handScore[0];

        // score dealer's hand
        handScore = game.score(keep0, turn, false);
        scores[dealer] += handScore[0];

        // score crib
        CribbageHand crib = new CribbageHand(throw0, throw1);
        handScore = game.score(crib, turn, true);
        scores[dealer] += handScore[0];

        return scores[0] - scores[1];


        // return game.gameValue(scores);
    }

    public static String getInfoSet(List<CribbageCard> myCards, boolean amDealer) {
        return (amDealer ? 1 : 0) + getSortedCardString(myCards);
    }

    /**
     * Get a String of sorted cards, including majority suit if exists
     * @param cards list
     * @return String representing the ranks of the shown cards
     */
    public static String getSortedCardString(List<CribbageCard> cards) {
        String flushSuit = getFlushSuit(cards);
        Collections.sort(cards, new SortCards(flushSuit));
        if (flushSuit == null) {
            return concatCardRanks(cards);
        }
        else {
            List<CribbageCard> suitedCards = new ArrayList<>();
            List<CribbageCard> otherCards = new ArrayList<>();
            for (CribbageCard c: cards) {
                if (String.valueOf(c.getSuit()).equals(flushSuit)) {
                    suitedCards.add(c);
                }
                else {
                    otherCards.add(c);
                }
            }

            return concatCardRanks(suitedCards) + "|" + concatCardRanks(otherCards);
        }

    }

    private static String concatCardRanks(List<CribbageCard> cards) {
        String s = "";
        for (CribbageCard c : cards) {
            s += c.getRank().toString();
        }
        return s;
    }

    /**
     * Comparator for cards using rankToVal mapping
     */
    static class SortCards implements Comparator<CribbageCard>
    {
        private String flushSuit;
        public SortCards(String flushSuit){
            super();
            this.flushSuit = flushSuit;
        }
        public int compare(CribbageCard a, CribbageCard b)
        {
            int aVal = rankToVal.get(a.getRank().toString());
            int bVal = rankToVal.get(b.getRank().toString());

            // put the cards of the majority suit to the very right of all cards with same rank
            if (flushSuit != null && b.getRank() == a.getRank()) return (String.valueOf(a.getSuit()).equals(flushSuit)) ? 1 : -1;
            return aVal - bVal;
        }
    }
}
