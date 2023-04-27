import cpsc474.CribbageGame;
import cpsc474.CribbageHand;
import cpsc474.IterTools;
import cpsc474.KeepPolicy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This throw policy uses Schell's tables to improve on cpsc474.GreedyThrower.
 * Values for the two tables are gotten from http://www.cribbageforum.com/SchellDiscard.htm
 */
public class SchellThrower implements KeepPolicy {
	private CribbageGame game;
	private List<List<Integer>> possibleThrows;

	private DealerTable dealerTable;
	private NonDealerTable nonDealerTable;

	public SchellThrower(CribbageGame game) {
		this.game = game;
		this.possibleThrows = new ArrayList<>();

		List<Integer> indices = new ArrayList<>();
		for (int i = 0; i < game.cardsDealt(); i++) {
			indices.add(i);
		}

		// make list of list of combinations of 2 indices for possible
		// indices of cards to throw
		IterTools.combinations(indices, game.cardsDealt() - game.cardsToKeep()).forEach(possibleThrows::add);

		dealerTable = new DealerTable();
		nonDealerTable = new NonDealerTable();


	}

	@Override
	public CribbageHand[] keep(CribbageHand cards, int[] scores, boolean amDealer) {
		CribbageHand[] bestSplit = null;
		double bestNet = Double.NEGATIVE_INFINITY;
		int ties = 0;
		for (List<Integer> throwIndices : possibleThrows) {
			CribbageHand[] currSplit = cards.split(throwIndices);
			double netPoints = game.score(currSplit[0], null, false)[0] + getSchellValue(currSplit[1], amDealer);
			if (netPoints > bestNet) {
				bestSplit = currSplit;
				bestNet = netPoints;
				ties = 0;
			} else if (netPoints == bestNet) {
				// retain ties with probability 1/#ties for uniform
				// random tiebreaking
				ties++;
				if (Math.random() < 1.0 / ties) {
					bestSplit = currSplit;
				}
			}
		}
		return bestSplit;
	}

	private double getSchellValue(CribbageHand discards, boolean amDealer) {
		//http://www.cribbageforum.com/SchellDiscard.htm
		String rank1 = discards.cards.get(0).getRank().getName();
		String rank2 = discards.cards.get(1).getRank().getName();
		if (amDealer) {
			return dealerTable.tableMap.get(rank1).get(rank2);
		}
		return -1 * nonDealerTable.tableMap.get(rank1).get(rank2);
	}


	public class DealerTable {
		HashMap<String, HashMap<String, Double>> tableMap;
		public DealerTable(){
			String[] headers = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "T", "J", "Q", "K"};
			double[][] data = {
					{5.38, 4.23, 4.52, 5.43, 5.45, 3.85, 3.85, 3.80, 3.40, 3.42, 3.65, 3.42, 3.41},
					{4.23, 5.72, 7.00, 4.52, 5.45, 3.93, 3.81, 3.66, 3.71, 3.55, 3.84, 3.58, 3.52},
					{4.52, 7.00, 5.94, 4.91, 5.97, 3.81, 3.58, 3.92, 3.78, 3.57, 3.90, 3.59, 3.67},
					{5.43, 4.52, 4.91, 5.63, 6.48, 3.85, 3.72, 3.83, 3.72, 3.59, 3.88, 3.59, 3.60},
					{5.45, 5.45, 5.97, 6.48, 8.79, 6.63, 6.01, 5.48, 5.43, 6.66, 7.00, 6.63, 6.66},
					{3.85, 3.93, 3.81, 3.85, 6.63, 5.76, 4.98, 4.63, 5.13, 3.17, 3.41, 3.23, 3.13},
					{3.85, 3.81, 3.58, 3.72, 6.01, 4.98, 5.92, 6.53, 4.04, 3.23, 3.53, 3.23, 3.26},
					{3.80, 3.66, 3.92, 3.83, 5.48, 4.63, 6.53, 5.45, 4.72, 3.80, 3.52, 3.19, 3.16},
					{3.40, 3.71, 3.78, 3.72, 5.43, 5.13, 4.04, 4.72, 5.16, 4.29, 3.97, 2.99, 3.06},
					{3.42, 3.55, 3.57, 3.59, 6.66, 3.17, 3.23, 3.80, 4.29, 4.76, 4.61, 3.31, 2.84},
					{3.65, 3.84, 3.90, 3.88, 7.00, 3.41, 3.53, 3.52, 3.97, 4.61, 5.33, 4.81, 3.96},
					{3.42, 3.58, 3.59, 3.59, 6.63, 3.23, 3.23, 3.19, 2.99, 3.31, 4.81, 4.79, 3.46},
					{3.41, 3.52, 3.67, 3.60, 6.66, 3.13, 3.26, 3.16, 3.06, 2.84, 3.96, 3.46, 4.58}
			};

			this.tableMap = new HashMap<>();

			for (int row = 0; row < headers.length; row++) {
				HashMap<String, Double> rowMap = new HashMap<>();
				for (int col = 0; col < headers.length; col++) {
					rowMap.put(headers[col], data[row][col]);
				}
				this.tableMap.put(headers[row], rowMap);
			}

		}

	}


	public class NonDealerTable {
		HashMap<String, HashMap<String, Double>> tableMap;
		public NonDealerTable() {
			String[] headers = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "T", "J", "Q", "K"};
			double[][] data = {{6.02, 5.07, 5.07, 5.72, 6.01, 4.91, 4.89, 4.85, 4.55, 4.48, 4.68, 4.33, 4.30},
					{5.07, 6.38, 7.33, 5.33, 6.11, 4.97, 4.97, 4.94, 4.70, 4.59, 4.81, 4.56, 4.45},
					{5.07, 7.33, 6.68, 5.96, 6.78, 4.87, 5.01, 5.05, 4.87, 4.63, 4.86, 4.59, 4.48},
					{5.72, 5.33, 5.96, 6.53, 7.26, 5.34, 4.88, 4.94, 4.68, 4.53, 4.85, 4.46, 4.36},
					{6.01, 6.11, 6.78, 7.26, 9.37, 7.47, 7.00, 6.30, 6.15, 7.41, 7.76, 7.34, 7.25},
					{4.91, 4.97, 4.87, 5.34, 7.47, 7.08, 6.42, 5.86, 6.26, 4.31, 4.57, 4.22, 4.14},
					{4.89, 4.97, 5.01, 4.88, 7.00, 6.42, 7.14, 7.63, 5.26, 4.31, 4.68, 4.32, 4.27},
					{4.85, 4.94, 5.05, 4.94, 6.30, 5.86, 7.63, 6.82, 5.83, 5.10, 4.59, 4.31, 4.20},
					{4.55, 4.70, 4.87, 4.68, 6.15, 6.26, 5.26, 5.83, 6.39, 5.43, 4.96, 4.11, 4.03},
					{4.48, 4.59, 4.63, 4.53, 7.41, 4.31, 4.31, 5.10, 5.43, 6.08, 5.63, 4.61, 3.88},
					{4.68, 4.81, 4.86, 4.85, 7.76, 4.57, 4.68, 4.59, 4.96, 5.63, 6.42, 5.46, 4.77},
					{4.33, 4.56, 4.59, 4.46, 7.34, 4.22, 4.32, 4.31, 4.11, 4.61, 5.46, 5.79, 4.49},
					{4.30, 4.45, 4.48, 4.36, 7.25, 4.14, 4.27, 4.20, 4.03, 3.88, 4.77, 4.49, 5.65}};

			this.tableMap = new HashMap<>();

			for (int row = 0; row < headers.length; row++) {
				HashMap<String, Double> rowMap = new HashMap<>();
				for (int col = 0; col < headers.length; col++) {
					rowMap.put(headers[col], data[row][col]);
				}
				this.tableMap.put(headers[row], rowMap);
			}

		}

	}
}
