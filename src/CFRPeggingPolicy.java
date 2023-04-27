import cpsc474.*;

import java.util.*;
import java.util.stream.Collectors;

public class CFRPeggingPolicy implements PegPolicy {
    HashMap<String, PegNode> nodes;
    PegPolicy backup;

    private boolean complete;

    private HashMap<String, Integer> rankToVal = new HashMap<>();

    public CFRPeggingPolicy(PegPolicy backupPolicy, HashMap<String, PegNode> nodes, boolean complete) {
        this.nodes = nodes;
        this.complete = complete;
        backup = backupPolicy;
        populateRanks();
    }

    public List<CribbageCard> copyCards(List<CribbageCard> cards) {
        List<CribbageCard> newList = new ArrayList<>();
        for (CribbageCard c: cards) {
            newList.add(new CribbageCard(c.getRank(), c.getSuit()));
        }
        return newList;
    }

    @Override
    public CribbageCard peg(CribbageHand cards, PeggingHistory hist, int[] scores, boolean amDealer)
    {
        List<CribbageCard> myCards = copyCards(cards.cards);
        String infoSet = getInfoSet(myCards, getCardsPlayed(hist, amDealer), getCardsPlayed(hist, !amDealer), getCurSeq(hist, true));

        // ensure we know what the legal actions are and return null if we do not have any
        boolean canPlay = false;
        List<CribbageCard> validActions = new ArrayList<>();
        for (CribbageCard c : cards) {
            if (hist.isLegal(c, amDealer ? 0 : 1)){
                canPlay = true;
                validActions.add(c);
            }
        }

        if (!canPlay) return null;

        // use CFR node if we have one for this infoset
        if (nodes.containsKey(infoSet)) {
            Collections.sort(myCards, new SortCards());
            int a = nodes.get(infoSet).getAction();
            CribbageCard chosenAction = myCards.get(a);
            return chosenAction;
        }

        // use greedy as backup
        return backup.peg(cards, hist, scores, amDealer);
    }



    /**
     * Get list of cards played in the current round
     * @param history
     * @return list of cards
     */
    public static List<CribbageCard> getCurSeq(PeggingHistory history, boolean considerStartRound) {
        List<CribbageCard> cards = new ArrayList<>();
        if (history.startRound() && considerStartRound) {
            //System.out.println("used");
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
     * @param dealer
     * @return list of cards
     */
    public static List<CribbageCard> getCardsPlayed(PeggingHistory history, boolean dealer) {
        List<CribbageCard> cards = new ArrayList<>();
        int historyPlayer = dealer ? 0 : 1;
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
    public String getInfoSet(List<CribbageCard> hand0, List<CribbageCard> plays0, List<CribbageCard> plays1, List<CribbageCard> curSeq) {
        if (this.complete) return getRankString(hand0, true) + "|" +  getRankString(plays0, true) + "|" + getRankString(plays1, true) + "|" + getRankString(curSeq, false);
        return getRankString(hand0, true) + "|" + getRankString(curSeq, false);// ranks;//  + "|" + getRankString(curSeq, false);*/

    }

    /**
     * Get a String of card ranks (ignoring suit) from inputted list
     * @param cards list
     * @param sort boolean
     * @return String representing the ranks of the shown cards
     */
    public String getRankString(List<CribbageCard> cards, boolean sort) {
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
    class SortCards implements Comparator<CribbageCard>
    {
        public int compare(CribbageCard a, CribbageCard b)
        {
            return rankToVal.get(a.getRank().toString()) - rankToVal.get(b.getRank().toString());
        }
    }

    public void populateRanks() {
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
}
