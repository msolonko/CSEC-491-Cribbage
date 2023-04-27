import cpsc474.*;

import java.util.ArrayList;
import java.util.List;

public class MCPegging implements PegPolicy {
    KeepPolicy keepPolicy;
    public MCPegging() {
        this.keepPolicy = new SchellThrower(new CribbageGame());
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

        boolean canPlay = false;
        for (CribbageCard c : cards) {
            if (hist.isLegal(c, amDealer ? 0 : 1)){
                canPlay = true;
            }
        }
        if (!canPlay) return null;

        List<CribbageCard> playedCards = getCardsPlayed(hist, 0, amDealer ? 0 : 1);
        List<CribbageCard> oppPlayedCards = getCardsPlayed(hist, 1, amDealer ? 0 : 1);
        playedCards.addAll(oppPlayedCards);



        return MonteCarlo.getBestMove(hist, new CribbageHand(myCards), amDealer, this.keepPolicy);
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



}
