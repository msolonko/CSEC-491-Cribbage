import cpsc474.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class for training CFR Pegging policy.
 */
public class CFRPegging {
    static CribbageGame game = new CribbageGame();
    static HashMap<String, Integer> rankToVal = new HashMap<>();
    static HashMap<String, PegNode> nodes = new HashMap<>();
    public static void main(String[] args) throws Exception {
        populateRanks();
        int starting_count = 0;//Integer.parseInt(args[0]);
        NodeLoader nodeLoader = new NodeLoader();

        // define keepPolicy using existing CFR throwing
        KeepPolicy keepPolicy = new CFRThrower(game, new GreedyThrower(game), nodeLoader.getThrowNodes("thrownodes_v2_1000k.txt"), false);

        if (starting_count > 0) {
            nodes = nodeLoader.getPegNodes("pegnodes_v2_" + starting_count + "k.txt");
        }

        int[] scores = new int[] {0, 0};
        for (int i = 0; i < 10000 * 1000 + 1; i++){
            cfr(null, 1.0, 1.0, scores, 0, keepPolicy, 0, null, 0);
            if (i % 10000 == 0) {
                System.out.println(i);
                System.out.println(nodes.size());
            }
            if (i % 200000 == 0 && i > 10000) {
                int iters = ( i) / 1000 + starting_count;
                nodeLoader.saveNodes("pegnodes_v2_" + (iters) + "k.txt", nodes);
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
     * The CFR Algorithm. Outputs point difference between player 0 and 1.
     * @param history
     * @param p0
     * @param p1
     * @param scores
     * @param roundNum (0 to start, 1 or 2 when going)
     * @param keepPolicy
     * @param dealer
     * @param playerHands
     * @param player
     * @return nodeUtil
     */
    public static double cfr(
        PeggingHistory history,
        double p0,
        double p1,
        int[] scores,
        int roundNum,
        KeepPolicy keepPolicy,
        int dealer,
        CribbageHand[] playerHands,
        int player
        ) {

        // stopping condition to ensure that we are saving info states where few cards left
        // use Greedy pegging strategy for the remainder

        // top line used for complex agent that makes all 3 moves (peg v3 or peg v4) - bottom for the simpler agent (peg v2 and peg v1)
        //if (roundNum > 0 && (playerHands[player].size() < 2 || playerHands[0].size() == 0 || playerHands[1].size() == 0)) {
        if (roundNum > 0 && (playerHands[player].size() <= 2 || playerHands[0].size() == 0 || playerHands[1].size() == 0)) {
            GreedyPegger pegger = new GreedyPegger();
            int turn = player;

            // play out the rest of the game using backup greedy policy
            while (!history.isTerminal()) {
                CribbageCard play = pegger.peg(playerHands[turn], history, null, turn == dealer);
                if (play != null) {
                    playerHands[turn] = playerHands[turn].remove(play);
                }
                history = history.play(play, turn == dealer ? 0 : 1);

                int[] playScore = history.getScore();
                if (playScore[0] > 0)
                {
                    scores[turn] += playScore[0];
                }
                else if (playScore[0] < 0)
                {
                    scores[1 - turn] += -playScore[0];
                }

                turn = 1 - turn;
            }
        }

        // return score difference
        if (history != null && history.isTerminal()) {
            int scoreDiff = scores[0] - scores[1];
            return scoreDiff;
        }

        // initializes everything at the start of the algorithm
        if (roundNum == 0) {
            roundNum += 1;
            history = new PeggingHistory(game);
            dealer = 1;

            // dealer pegs second, so player to start is not the dealer
            player = 1 - dealer;

            // deal cards
            CribbageHand[] cardsInPlay = game.deal();

            // get keep/throw for each player
            CribbageHand[][] keeps = new CribbageHand[2][];
            for (int p = 0; p < 2; p++)
                {
                keeps[p] = keepPolicy.keep(cardsInPlay[p], null, p == dealer);

                // error checking
                if (!cardsInPlay[p].isLegalSplit(keeps[p]))
                    {
                    throw new RuntimeException(Arrays.toString(keeps[p]) + " does not partition " + cardsInPlay[p]);
                    }
                else if (keeps[p][0].size() != 4)
                    {
                    throw new RuntimeException("Invalid partition sizes " + Arrays.toString(keeps[p]));
                    }
                }

            playerHands = new CribbageHand[]{keeps[0][0], keeps[1][0]};

            // need sorted for correct infoset string making
            Collections.sort(playerHands[0].cards, new SortCards());
            Collections.sort(playerHands[1].cards, new SortCards());
        }

        String infoSet = getInfoSet(playerHands[player].cards, getCardsPlayed(history, player, dealer), getCardsPlayed(history, 1-player, dealer), getCurSeq(history));

        double param = player == 0 ? p0 : p1;

        // get legal actions
        List<CribbageCard> actions = new ArrayList<>();
        for (CribbageCard c : playerHands[player]) {
            if (history.isLegal(c, player == dealer ? 0 : 1)){
                actions.add(c);
            }
        }

        PegNode node;
        if (!nodes.containsKey(infoSet)) {
            // create a new infoSet
            node = new PegNode((byte) actions.size());
            if (actions.size() > 0) nodes.put(infoSet, node);
        }
        else {
            node = nodes.get(infoSet);
        }

        if (node.numActions != actions.size()) {
            throw new RuntimeException("CFR Node and legal actions disagree on # of valid actions. CFR Node: " + infoSet + " " + node.numActions + ". Possible actions: " + actions);
        }

        // for the time we cannot play a card
        if (actions.size() == 0) {
            actions.add(null);
        }

        // initialize util array
        float[] strategy = node.getStrategy(param);
        double[] util = new double[actions.size()];
        for (int i = 0; i < actions.size(); i++) {
            util[i] = 0.0;
        }

        double nodeUtil = 0;
        for (int i = 0; i < actions.size(); i++) {
            PeggingHistory nextHistory = history.play(actions.get(i), player == dealer ? 0 : 1);

            // make new hands, removing cards from hand if played
            CribbageHand nextHand0 = playerHands[0];
            CribbageHand nextHand1 = playerHands[1];

            if (actions.get(i) != null) {
                if (player == 0) {
                    nextHand0 = playerHands[0].remove(actions.get(i));
                } else {
                    nextHand1 = playerHands[1].remove(actions.get(i));
                }
            }

            int[] newScores = scores.clone();

            // score the play
			int[] playScore = nextHistory.getScore();
			if (playScore[0] > 0)
			    {
                    newScores[player] += playScore[0];
			    }
			else if (playScore[0] < 0)
			    {
                    newScores[1 - player] += -playScore[0];
			    }

            double strat_i = 1.0;
            if (actions.get(i) != null){
                strat_i = strategy[i];
            }


            // call CFR recursively
            if (player == 0) {
                util[i] = cfr(nextHistory, p0 * strat_i, p1, newScores, roundNum, keepPolicy, dealer, new CribbageHand[]{nextHand0, nextHand1}, 1-player);
            }
            else {
                util[i] = -cfr(nextHistory, p0, p1 * strat_i, newScores, roundNum, keepPolicy, dealer, new CribbageHand[]{nextHand0, nextHand1}, 1-player);
            }

            nodeUtil += strat_i * util[i];
        }

        // update regretSums
        if (node.numActions > 0) {
            for (int i = 0; i < actions.size(); i++) {
                double regret = util[i] - nodeUtil;
                node.regretSum[i] += regret * (player == 0 ? p1 : p0);
            }
        }

        return player == 0 ? nodeUtil : -nodeUtil;
    }



    /**
     * Get list of cards played in the current round
     * @param history
     * @return list of cards
     */
    public static List<CribbageCard> getCurSeq(PeggingHistory history) {
        List<CribbageCard> cards = new ArrayList<>();

        if (history.startRound()) {
            return cards;
        }

        while (history != null) {
            CribbageCard card = history.card;
            if (card != null) {
                cards.add(card);
            }
            history = history.prevPlay;
        }
        Collections.reverse(cards);
        return cards;
    }

    /**
     * Get all cards played by a specific player in the current crib/pegging round.
     * @param history
     * @param player
     * @param dealer
     * @return list of cards
     */
    public static List<CribbageCard> getCardsPlayed(PeggingHistory history, int player, int dealer) {
        List<CribbageCard> cards = new ArrayList<>();
        int historyPlayer = player == dealer ? 0 : 1;
        while (history != null) {
            CribbageCard card = history.card;
            if (card != null && history.player == historyPlayer) {
                // if the player matches the player of interest, add that card
                cards.add(card);
            }
            if (history.prevPlay == null) {
                // go to previous round if no more rounds in this one
                history = history.prevRound;
            }
            else {
                history = history.prevPlay;
            }
        }
        return cards;
    }


    /**
     * Combine current information into a string representation of infoset.
     *
     * @param hand0: hand of hero
     * @param plays0: plays of hero
     * @param plays1: plays of opponent
     * @param curSeq: current sequence of cards on the board
     * @return String representation of infoset
     */
    public static String getInfoSet(List<CribbageCard> hand0, List<CribbageCard> plays0, List<CribbageCard> plays1, List<CribbageCard> curSeq) {
        // this is for the complete agent
        return getRankString(hand0, true) + "|" +  getRankString(plays0, true) + "|" + getRankString(plays1, true) + "|" + getRankString(curSeq, false);

        // this is for the incomplete agent (does not consider cards previously played)
        //return getRankString(hand0, true) + "|" + getRankString(curSeq, false);

    }
    /**
     * Get a String of card ranks (ignoring suit) from inputted list
     * @param cards list
     * @param sort boolean
     * @return String representing the ranks of the shown cards
     */
    public static String getRankString(List<CribbageCard> cards, boolean sort) {
        if (sort){
            Collections.sort(cards, new SortCards());
        }
        List<CardRank> ranks = cards.stream().map(CribbageCard::getRank).collect(Collectors.toList());
        List<String> rankStr = ranks.stream().map(CardRank::getName).collect(Collectors.toList());
        return String.join("", rankStr);
    }


    /**
     * Comparator for cards using rankToVal mapping
     */
    static class SortCards implements Comparator<CribbageCard>
    {
        public int compare(CribbageCard a, CribbageCard b)
        {
            return rankToVal.get(a.getRank().toString()) - rankToVal.get(b.getRank().toString());
        }
    }

}