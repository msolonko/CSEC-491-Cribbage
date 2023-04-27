import cpsc474.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MonteCarlo {
    public static CribbageCard getBestMove(PeggingHistory history, CribbageHand hand, boolean amDealer, KeepPolicy keepPolicy)  {

        // if no decisions to make, return only move
        List<CribbageCard> cardList = new ArrayList<>();
        for (CribbageCard c : hand) {
            if (history.isLegal(c, amDealer ? 0 : 1)) {
                cardList.add(c);
            }
        }
        if (cardList.size() == 0) return null;
        if (cardList.size() == 1) return cardList.get(0);

        // create valid deck
        List<CribbageCard> cardsSeen = new ArrayList<>(hand.cards);
        cardsSeen.addAll(getCardsPlayed(history, true));
        cardsSeen.addAll(getCardsPlayed(history, false));

        CribbageDeck deck = new CribbageDeck();
        deck.shuffle();
        deck.remove(cardsSeen);

        double bestResult = Double.NEGATIVE_INFINITY;
        CribbageCard bestMove = null;
        for (CribbageCard action : cardList) {
            PeggingHistory newHist = history.play(action, amDealer ? 0 : 1);
            CribbageHand newHand = hand.remove(action);
            double res = simulate(newHist, newHand, amDealer, deck,keepPolicy,20);
          //  System.out.println("cpsc474.Card: " + action + ". Value: " + res);
            if (res > bestResult) {
                bestResult = res;
                bestMove = action;
            }

        }
        //System.out.println("Best move: " + bestMove);
        return bestMove;
    }

    public static double simulate(PeggingHistory history, CribbageHand hand, boolean amDealer, CribbageDeck deck, KeepPolicy keepPolicy, int n) {
        int pointDiff = 0;
        int countInvalid = 0;
        for (int i = 0; i < n; i++) {
            try {
                double val = simulateHelper(history, hand, deck, amDealer, keepPolicy, 0);
                if (val == Double.NEGATIVE_INFINITY) {
                    countInvalid++;
                } else {
                    pointDiff += val;
                }
            }
            catch (IOException exception) {
                System.out.println(exception);
            }
        }
        return ((double) pointDiff) / (n - countInvalid + 0.1); // to avoid divide by 0 errors
    }

    public static double simulateHelper(PeggingHistory history, CribbageHand hand, CribbageDeck deck, boolean amDealer, KeepPolicy keepPolicy, int depth) throws IOException {
        //System.out.println("new sim");

        // it has tried 10 times to generate valid cards and failed - just quit
        if (depth==10) {
            return Double.NEGATIVE_INFINITY;
        }
        deck.shuffle();



        CribbageHand oppHand = null;// = new cpsc474.CribbageHand(deck.peek(numCardsOpp));
        List<CribbageCard> oppPlayed = getCardsPlayed(history, !amDealer);
        int numCardsOpp = 4 - oppPlayed.size();

        int whileAttempsLeft = 5;
        while(whileAttempsLeft > 0) {
            whileAttempsLeft--;
            deck.shuffle();
            List<CribbageCard> otherCards = deck.peek(numCardsOpp + 2); // including crib cards
            otherCards.addAll(oppPlayed);
            CribbageHand[] keeps = keepPolicy.keep(new CribbageHand(otherCards), null, !amDealer);
            boolean discardedPlayed = false;

            // ensure that the cards that we played are not discarded by the keepPolicy
            for (CribbageCard c : oppPlayed) {
                if (c == keeps[1].cards.get(0) || c == keeps[1].cards.get(1)) {
                    discardedPlayed = true;
                }
            }

            if (!discardedPlayed) {
                oppHand = keeps[0];
                break;
            }

        }

        if (oppHand == null) {
          oppHand  = new CribbageHand(deck.peek(numCardsOpp));
        }

        CribbageHand myHand = new CribbageHand(hand.cards);
        CribbageHand[] pegHands = new CribbageHand[]{myHand, oppHand};
        PeggingHistory hist = history;
        int turn = 1; // 0 will mean the hero
        PegPolicy pegPolicy = new RulePegger(new CribbageGame());
        int[] scores = {0, 0};

       // System.out.println("Random play initial info");
       /*System.out.println("My hand: " + myHand);
        System.out.println("cpsc474.Card I played before: " + getCardsPlayed(history, amDealer));
        System.out.println("cpsc474.Card opp played before: " + getCardsPlayed(history, !amDealer));
       System.out.println("Opp hand (random): " + oppHand);*/



        while (!hist.isTerminal())
        {
            CribbageCard play;

            // this can error out for various reasons like invalid play due to the sampling
            // if this happens, just try again by running simulateHelper
            try {
                if (turn == 0) {
                    play = pegPolicy.peg(pegHands[0], hist, null, amDealer);
                }
                else {
                    play = pegPolicy.peg(pegHands[1], hist, null, !amDealer);
                }
            }
            catch(Exception e) {
                return simulateHelper(history, hand, deck, amDealer, keepPolicy, depth+1);
            }


            //System.out.println("Cur seq: " + getCurSeq(hist));
           // System.out.println("Turn: " + turn + ". Going to play: " + play);

            // check for legality of chosen card
            int playerArg = (turn == 0 && amDealer || turn == 1 && !amDealer) ? 0 : 1;
            if (play == null && hist.hasLegalPlay(pegHands[turn], playerArg))
            {
                throw new RuntimeException("passing when " + pegHands[turn] + " contains a valid card");
            }
            else if (play != null && !hist.isLegal(play, playerArg))
            {
                throw new RuntimeException("chosen card " + play + " us not legal");
            }

            hist = hist.play(play, playerArg);
            if (hist == null) {
                // this happens when opponent has passed, but the random cards they were just given
                // would not have passed there. If this is the case, just discard this simulation and run again.
                //System.out.println("Somehow there is an issue here");
               // System.out.println("Played: " + play);
                //System.out.println("Turn: " + turn);
                //System.out.println("Hand0: " + pegHands[0] + ". Hand1: " + pegHands[1]);
                return simulateHelper(history, hand, deck, amDealer, keepPolicy, depth+1); // run again instead
            }

            // score the play
            int[] playScore = hist.getScore();
            if (playScore[0] > 0)
            {
                scores[turn] += playScore[0];
            }
            else if (playScore[0] < 0)
            {
                scores[1 - turn] += -playScore[0];
            }

            // remove played card from hand
            if (play != null)
            {

                CribbageHand newHand = pegHands[turn].remove(play);

                if (newHand == null)
                {
                    throw new RuntimeException("played card " + play + " not in hand " + pegHands[turn]);
                }
                pegHands[turn] = newHand;
            }

            // next player's turn
            turn = turn == 0 ? 1 : 0;
        }

        return scores[0] - scores[1];
    }



    public static List<CribbageCard> getCardsPlayed(PeggingHistory history, boolean isDealer) {
        List<CribbageCard> cards = new ArrayList<>();
        int historyPlayer = isDealer ? 0 : 1;
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
