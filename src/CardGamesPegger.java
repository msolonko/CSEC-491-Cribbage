// Import required classes
import cpsc474.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/*
This is the cardgames.io "Bill" pegging policy
 */
public class CardGamesPegger implements PegPolicy {
	// Inner class to store card and score as a play option
	private class PlayOption {
		CribbageCard card;
		int score;

		PlayOption(CribbageCard card) {
			this.card = card;
		}

		int getScore() {
			return score;
		}
	}

	// Override peg method to implement custom logic
	@Override
	public CribbageCard peg(CribbageHand cards, PeggingHistory hist, int[] scores, boolean amDealer) {
		List<CribbageCard> legalCards = new ArrayList<>();

		// Check if a card is legal to play and add it to the legalCards list
		for (CribbageCard c : cards) {
			if (hist.isLegal(c, amDealer ? 0 : 1)) {
				legalCards.add(c);
			}
		}

		// Forced moves
		if (legalCards.size() == 0) {
			return null;
		}
		if (legalCards.size() == 1) {
			return legalCards.get(0);
		}

		CribbageCard bestPlay = null;
		int bestScore = Integer.MIN_VALUE;
		boolean tie = false;

		// Determine the best play based on the score
		for (CribbageCard c : legalCards) {
			int[] score = hist.score(c, amDealer ? 0 : 1);
			if (score[0] > bestScore) {
				bestScore = score[0];
				bestPlay = c;
				tie = false;
			} else if (score[0] == bestScore) {
				tie = true;
			}
		}

		// return bestPlay if no ties
		if (!tie) {
			return bestPlay;
		}

		// calculate potential future scores
		List<PlayOption> options = new ArrayList<>();
		CribbageGame game = new CribbageGame();

		// Calculate the score of each play option and add it to the options list
		for (CribbageCard c : legalCards) {
			PlayOption option = new PlayOption(c);
			int score = game.rankValue(c.getRank()); // playing higher cards preferred
			int seqVal = hist.startRound() ? 0 : hist.getTotal();
			int valueAfterCard = game.rankValue(c.getRank()) + seqVal;

			if (valueAfterCard == 5 || valueAfterCard == 21) {
				score -= 15;
			}

			option.score = score;
			options.add(option);
		}

		// Sort the options in descending order of score
		options.sort(Comparator.comparingInt(CardGamesPegger.PlayOption::getScore).reversed());
		return options.get(0).card; // best option
	}
}
