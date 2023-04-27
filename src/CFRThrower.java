import cpsc474.CribbageCard;
import cpsc474.CribbageGame;
import cpsc474.CribbageHand;
import cpsc474.KeepPolicy;

import java.util.*;


public class CFRThrower implements KeepPolicy
{
    private CribbageGame game;
    private KeepPolicy backup;
    HashMap<String, ThrowNode> nodes;

    private boolean suited;
    static HashMap<String, Integer> rankToVal = new HashMap<>();
    public CFRThrower(CribbageGame game, KeepPolicy backup, HashMap<String, ThrowNode> nodes, boolean suited)
    {
        this.game = game;
        this.backup = backup;
        this.nodes = nodes;
        this.suited = suited;
        populateRanks();

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

    public List<CribbageCard> copyCards(List<CribbageCard> cards) {
        List<CribbageCard> newList = new ArrayList<>();
        for (CribbageCard c: cards) {
            newList.add(new CribbageCard(c.getRank(), c.getSuit()));
        }
        return newList;
    }

    @Override
    public CribbageHand[] keep(CribbageHand cards, int[] scores, boolean amDealer)
    {
        List<CribbageCard> myCards = copyCards(cards.cards);
        String infoSet = getInfoSet(myCards, amDealer);

        if (nodes.containsKey(infoSet)) {
            Collections.sort(myCards, new SortCards(getFlushSuit(myCards)));
            int[] a = nodes.get(infoSet).getAction();
            return new CribbageHand(myCards).split(new ArrayList<>(Arrays.asList(a[0], a[1])));
        }


        return backup.keep(cards, scores, amDealer);
    }



    public String getInfoSet(List<CribbageCard> myCards, boolean amDealer) {
        return (amDealer ? 1 : 0) + getSortedCardString(myCards);
    }


    /**
     * Get a String of sorted cards, including majority suit if exists
     * @param cards list
     * @return String representing the ranks of the shown cards, with divider if a flush is possible.
     */
    public String getSortedCardString(List<CribbageCard> cards) {
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

    /**
     *
     * @param cards
     * @return flushSuit if exists (4 or more cards share this suit)
     */
    private String getFlushSuit(List<CribbageCard> cards) {
        if (!this.suited) return null;
        HashMap<String, Integer> suitCounts = new HashMap<>();
        for (CribbageCard c : cards) {
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
        return flushSuit;
    }
}
