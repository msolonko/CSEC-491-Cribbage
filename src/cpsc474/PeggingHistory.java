package cpsc474;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

/**
 * A history of the cards played durring the pegging (counting) phase
 * of one hand of cribbage.
 *
 * @author Jim Glenn
 * @version 0.1 2022-07-18 cloudy day over the Adirondacks
 */
public class PeggingHistory
{
    /**
     * The last play in the previous round (counting from 0 to 31)
     * of pegging.
     */
    public PeggingHistory prevRound;

    /**
     * The last play in this round of pegging.
     */
    public PeggingHistory prevPlay;

    /**
     * The card played in thisstep of pegging, or null.
     */
    public CribbageCard card;

    /**
     * The player who played at this step of pegging; 0 for the dealer
     * and 1 for the mnn-dealer.
     */
    public int player;

    /**
     * An array indicating which players have passed during this round.
     */
    private boolean[] passed;

    /**
     * The current count in the current round of pegging.
     */
    private int total;
    
    /**
     * The number of cards played for each player in this hand of pegging.
     */
    private int[] cardsPlayed;
    
    /**
     * The game rules followed for this hand of pegging.
     */
    private CribbageGame game;

    /**
     * The number of points earned by the player who played the card at
     * this step of pegging.  Negative indicated points awarded to the
     * other player, as for a point for "go".  The five entries are the
     * total, points for pairs, fifteens, runs, and last card (including 31).
     */
    private int[] score;
    
    /**
     * Creates the initial hostory of a hand of pegging before either player
     * has played a card.
     */
    public PeggingHistory(CribbageGame game)
    {
	this(game, null, null, 0, new boolean[] {false, false}, new int[] {0, 0}, null, -1, null);
    }

    /**
     * Creates a pegging history with the given attributes.
     */
    private PeggingHistory(CribbageGame game, PeggingHistory prevPlay, PeggingHistory prevRound, int total, boolean[] passed, int[] played, CribbageCard card, int player, int[] score)
    {
	this.game = game;
	this.prevPlay = prevPlay;
	this.prevRound = prevRound;
	this.passed = passed;
	this.cardsPlayed = played;
	this.card = card;
	this.player = player;
	this.total = total;
	this.score = score;
    }

    /**
     * Reports whether this step was the last step in pegging.  This step
     * is the last if the last player with a card played it.
     *
     * @return true if this step was terminal; false otherwise
     */
    public boolean isTerminal()
    {
	return Arrays.stream(cardsPlayed).sum() == game.cardsToKeep() * 2;
    }

    /**
     * Returns the number of points earned by the player who played the card at
     * this step of pegging.  Negative values indicate points awarded to the
     * other player, as for a point for "go".  The behavior is undefined
     * if this is the initial history for a hand.
     *
     * @return a five element array giving the net points awarded to the
     * player who played a card at this step, in the order total,
     * pairs, fifteens, runs, and last card
     */
    public int[] getScore()
    {
	return Arrays.copyOf(score, score.length);
    }

    /**
     * Returns the total after this step of pegging.  The total is before
     * any reset, so will be positive after the 2nd player has passed
     * for the first time or after a count of 31.
     *
     * @return the current total
     */
    public int getTotal()
    {
	return total;
    }

    /**
     * Reports whether the next step of pegging will be the first card
     * played in a round.
     *
     * @return true if the next set is the start of a new count
     */
    public boolean startRound()
    {
	// initial history or 31 or both players passed
	return total == 0 || total == game.getPeggingLimit() || (passed[0] && passed[1]);
    }

    /**
     * Reports whether the given player has passed during the current
     * round of pegging.
     *
     * @param p 0 for the dealer, 1 for the non-dealer
     * @return true if the player has passed; false otherwise
     */
    public boolean hasPassed(int p)
    {
	return passed[p];
    }

    /**
     * Returns the hostory that results from the given player playing the
     * given card after this step.  If the play is illegal then thr return
     * value is null.
     *
     * @param card the card to play
     * @param player 0 for the dealer, 1 for the non-dealer
     * @return the resulting history, or null
     */
    public PeggingHistory play(CribbageCard card, int player)
    {
	if (this.player != -1 && player != 1 - this.player)
	    {
		// wrong player
		System.out.println("Wrong player");
		return null;
	    }
	
	int[] nextScore = score(card, player);
	if (nextScore == null)
	    {
		// illegal card
		System.out.println("illegal card");
			// TODO commenting because this happens in MonteCarlo and it is rerun in that case, so this issue is handled
		return null;
	    }

	// update total
	int nextTotal = startRound() ? 0 : total;
	if (card != null)
	    {
		nextTotal += game.rankValue(card.getRank());
	    }
	if (nextTotal > game.getPeggingLimit())
	    {
		return null;
	    }

	// update cards played
	int[] nextCardsPlayed = Arrays.copyOf(cardsPlayed, cardsPlayed.length);
	if (card != null)
	    {
		nextCardsPlayed[player] += 1;
	    }

	// updated passes
	boolean[] nextPassed = startRound() ? new boolean[] {false, false} : Arrays.copyOf(passed, passed.length);
	if (card == null)
	    {
		nextPassed[player] = true;
	    }

	PeggingHistory nextPrevRound, nextPrevPlay;
	if (startRound())
	    {
		nextPrevPlay = null;
		nextPrevRound = this;
	    }
	else
	    {
		nextPrevPlay = this;
		nextPrevRound = this.prevRound;
	    }
	
	return new PeggingHistory(game, nextPrevPlay, nextPrevRound, nextTotal, nextPassed, nextCardsPlayed, card, player, nextScore);
    }

    /**
     * Determines if the given card is legal for the given player to play.
     *
     * @param card a cpsc474.CribbageCard, non-null
     * @param player 0 for the dealer, 1 for the non-dealer
     * @return true if the card is legal, false otherwise
     */
    public boolean isLegal(CribbageCard card, int player)
    {
	return (startRound() ? 0 : total) + game.rankValue(card.getRank()) <= game.getPeggingLimit();
    }

    /**
     * Determines if the given hand contains a card that is legal for the
     * given player to play.
     *
     * @param hand a cpsc474.CribbageHand
     * @param player 0 for the dealer, 1 for the non-dealer
     * @return true if the hand contains a legal card to play, false otherwise
     */
    public boolean hasLegalPlay(CribbageHand hand, int player)
    {
	if (passed[player])
	    {
		return false;
	    }
	for (CribbageCard c : hand)
	    {
		if (isLegal(c, player))
		    {
			return true;
		    }
	    }
	return false;
    }

	public List<CribbageCard> getLegalActions(CribbageHand hand, int player) {
		List<CribbageCard> cards = new ArrayList<>();
		if (passed[player]){
			return cards; // Just returning null? Could this be hand.cards?
	    }
		for (CribbageCard c : hand) {
			if (isLegal(c, player)){
				cards.add(c);
		    }
		}
		return cards;
	}
    
    /**
     * Returns the score earned by the given player when making the given
     * play.  The score is negative to indicate that the other player scores
     * points (as for a "go")  The first element of the returned array
     * is the total score and the remaining elements are the subscores
     * in the order pairs, fifteens, runs, and last.
     *
     * @param card a cpsc474.CribbageCard, or null to indicate a pass ("go")
     * @param player 0 for the dealer, 1 for the non-dealer
     * @return a non-null five element array whose non-zero elements
     * are all of the same sign and whose first element is the sum of
     * other elements
     */
    public int[] score(CribbageCard card, int player)
    {
		//println("cpsc474.Card: " + card + ". Player:" + player );
	if (card == null)
	    {
		if (passed[player])
		    {
			// player has already passed
			return new int[] {0, 0, 0, 0, 0};
		    }
		else if (!passed[1 - player])
		    {
			// "go"
			return new int[] {-1, 0, 0, 0, -1};
		    }
		else
		    {
			// other player already conceded "go"
			return new int[] {0, 0, 0, 0, 0};
		    }
	    }
	else if (!startRound() && passed[player])
	    {
		// player has already passed but is playing a card
			//	System.out.println("Passed but playing");
		return null;
	    }

	// reset total at start of a round
	int prevTotal = startRound() ? 0 : total;

	if (prevTotal + game.rankValue(card.getRank()) > game.getPeggingLimit())
	    {
		// card puts count over 31
		//		System.out.println("Over 31");
		return null;
	    }

	// assert(card != null)

	// initialized tracking variables for card being played
	int countPlayed = 1; // number of cards examined (starting with param)
	int currMatches = 1; // number of consecutive cards that match
	int maxMatches = 1; // maximum number of matches seen
	int maxStraight = 1; // maximum run seen
	CardRank minRank = card.getRank(); // min rank seen
	CardRank maxRank = card.getRank(); // max rank seen
	boolean doubles = false; // whether a duplicate has been seen (breaking a run)
	Set<CardRank> ranksSeen = new HashSet<>(); // ranks seen
	ranksSeen.add(card.getRank());

	// scan history backwards to determine runs and pairs starting with
	// the current step
	PeggingHistory curr = startRound() ? null : this;
	// stop at beginning of round or when run not possible and not still
	// matching rank
	while (curr != null && (countPlayed == 0 || currMatches == maxMatches || !doubles))
	    {
		if (curr.card != null)
		    {
			countPlayed += 1;

			if (card.getRank().equals(curr.card.getRank()))
			    {
				// ranks match
				if (currMatches != -1)
				    {
					currMatches += 1;
				    }
				maxMatches = Math.max(maxMatches, currMatches);
			    }
			else
			    {
				// ranks don't match -- ignore for rest of history
				currMatches = -1;
			    }
			
			// update min/max rank
			if (curr.card.getRank().intValue() < minRank.intValue())
			    {
				minRank = curr.card.getRank();
			    }
			else if (curr.card.getRank().intValue() > maxRank.intValue())
			    {
				maxRank = curr.card.getRank();
			    }

			// update ranks/duplicates seen
			if (ranksSeen.contains(curr.card.getRank()))
			    {
				doubles = true;
			    }
			else
			    {
				ranksSeen.add(curr.card.getRank());
			    }

			if (!doubles && maxRank.intValue() - minRank.intValue() + 1 == countPlayed)
			    {
				// no duplicates, max-min+1 == count -> run
				maxStraight = countPlayed;
			    }
			
		    }
		
		curr = curr.prevPlay;
	    }
	
	int pairScore = game.pegPairValue(maxMatches);
	int straightScore = game.pegStraightValue(maxStraight);
	int fifteenScore = game.pegSumValue(prevTotal + game.rankValue(card.getRank()));
	int lastScore = 0;
	if (prevTotal + game.rankValue(card.getRank()) == game.getPeggingLimit())
	    {
		lastScore = game.pegExactValue(passed[1 - player]);
	    }
	else if (Arrays.stream(cardsPlayed).sum() + 1 == 2 * game.cardsToKeep())
	    {
		// eighth card played
		lastScore = 1;
	    }
	
	return new int[] {pairScore + fifteenScore + straightScore + lastScore, pairScore, fifteenScore, straightScore, lastScore};
    }
}
