// Import required classes
import cpsc474.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/*
This is the cardgames.io "Bill" throwing policy
 */
public class CardGamesThrower implements KeepPolicy {
	private CribbageGame game;
	private List<List<Integer>> possibleThrows;

	// Constructor for CardGamesThrower class
	public CardGamesThrower(CribbageGame game) {
		this.game = game;
		this.possibleThrows = new ArrayList<>();

		List<Integer> indices = new ArrayList<>();
		for (int i = 0; i < game.cardsDealt(); i++) {
			indices.add(i);
		}

		// Make a list of lists of combinations of 2 indices for possible card throw indices
		IterTools.combinations(indices, game.cardsDealt() - game.cardsToKeep()).forEach(possibleThrows::add);
	}

	// Inner class to store CribbageOption information
	private class CribbageOption {
		CribbageHand keepHand;
		CribbageHand throwHand;
		int score;
		int playRank = 0;

		CribbageOption(CribbageHand keepHand, CribbageHand throwHand, int score) {
			this.keepHand = keepHand;
			this.throwHand = throwHand;
			this.score = score;
		}

		int getPlayRank() {
			return playRank;
		}
	}

	// Adjust the number according to the crib ownership
	public int plusIfMyCrib(int nr, boolean isMyCrib) {
		return isMyCrib ? nr : -nr;
	}

	// Override keep method to implement custom logic
	@Override
	public CribbageHand[] keep(CribbageHand cards, int[] scores, boolean amDealer) {
		int bestNet = Integer.MIN_VALUE;
		List<CribbageOption> options = new ArrayList<>();

		// Save all possible splits to options list
		for (List<Integer> throwIndices : possibleThrows) {
			CribbageHand[] currSplit = cards.split(throwIndices);
			int score = game.score(currSplit[0], null, false)[0] + (amDealer ? 1 : -1) * game.score(currSplit[1], null, true)[0];
			options.add(new CribbageOption(currSplit[0], currSplit[1], score));
			bestNet = Math.max(bestNet, score);
		}

		final int finalBestNet = bestNet;
		List<CribbageOption> tiedOptions = options.stream()
				.filter(option -> option.score == finalBestNet)
				.collect(Collectors.toList());

		// Calculate playRank for each tied option
		// estimates potential to make straights, flushes, etc
		for (CribbageOption option : tiedOptions) {
			int playRank = 0;
			CribbageCard c1 = option.throwHand.cards.get(0);
			CribbageCard c2 = option.throwHand.cards.get(1);
			int c1Rank = c1.getRank().intValue();
			int c2Rank = c2.getRank().intValue();

			playRank += c1Rank;
			playRank += c2Rank;

			if (Math.abs(c1Rank - c2Rank) == 1) {
				playRank += plusIfMyCrib(5, amDealer);
			}
			if (c1.getSuit() == c2.getSuit()) {
				playRank += plusIfMyCrib(5, amDealer);
			}
			if (game.rankValue(c1.getRank()) == 5) {
				playRank += plusIfMyCrib(10, amDealer);
			}
			if (game.rankValue(c2.getRank()) == 5) {
				playRank += plusIfMyCrib(10, amDealer);
			}
			if (c1Rank == c2Rank) {
				playRank += plusIfMyCrib(25, amDealer);
			}

			if (game.rankValue(c1.getRank()) + game.rankValue(c2.getRank()) == 15) {
				playRank += plusIfMyCrib(25, amDealer);
			}

			option.playRank = playRank;
		}

		// Sort the tied options in descending order of playRank
		tiedOptions.sort(Comparator.comparingInt(CribbageOption::getPlayRank).reversed());
		CribbageHand throwHand = tiedOptions.get(0).throwHand;
		CribbageHand keepHand = tiedOptions.get(0).keepHand;

		return new CribbageHand[]{keepHand, throwHand};
	}
}