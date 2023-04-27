package cpsc474;

import java.util.Map;
import java.util.HashMap;

/**
 * The results of evaluating two cribbage policies in head-to-head
 * competetion.
 */
public class EvaluationResults
{
    /**
     * The number of games reflected in these results.
     */
    private int count;

    /**
     * The total points won by P0 and P1 in the games reflected by these
       results.
     */
    private long[] totalPoints;

    /**
     * The frequency of each result over the games reflected by these
     * results.
     */
    private Map<Integer, Integer> freq;

    /**
     * The total number of hands played over all the games reflected
     * by these results.
     */
    private long totalHands;
    
    public EvaluationResults()
    {
	count = 0;
	totalPoints = new long[2];
	freq = new HashMap<>();
    }

    /**
     * Updates these results for the results of one game.
     *
     * @param gameScore the game points won by P0 (negative for a loss)
     * @param hands the number of hands played
     */
    public void update(int gameScore, int hands)
    {
	int winner = gameScore > 0 ? 0 : 1;
	int pointsWon = winner == 0 ? gameScore : -gameScore;
	totalPoints[winner] += pointsWon;

	if (!freq.containsKey(gameScore))
	    {
		freq.put(gameScore, 0);
	    }
	freq.put(gameScore, freq.get(gameScore) + 1);

	count++;
	totalHands += hands;
    }

    /**
     * Returns the average net points per game won by P0.
     * Undefined if these results reflect 0 games.
     *
     * @return the average net points per game won by P0
     */
    public double averageNet()
    {
	return playerAverage(0) - playerAverage(1);
    }

    /**
     * Returns the average game points per game won by the given player.
     * Undefined if these results reflect 0 games.
     *
     * @param player 0 or 1
     * @return the average game points per game won by the corresponding player
     */
    public double playerAverage(int player)
    {
	return (double)totalPoints[player] / count;
    }

    /**
     * Returns the frequency of each game result from the point
     * of view of P0.  The possible values of game points will be
     * the keys and the corresponding frequencies are the values.
     * If a possible game result did not occur then it may not
     * exist as a key in the map.
     *
     * @return the frequency of each game result
     */
    public Map<Integer, Integer> getFrequency()
    {
	return new HashMap<>(freq);
    }

    /**
     * Returns the average number of hands per game over the games
     * reflected in these results.  Undefined if these games reflect
     * 0 games.
     */
    public double averageHands()
    {
	return (double)totalHands / count;
    }
    
    /**
     * Returns a printable representation of these results.
     *
     * @return a printable representation of these results
     */
    public String toString()
    {
	StringBuilder output = new StringBuilder();
    double agent1WinRate = (double)(freq.getOrDefault(3, 0) + freq.getOrDefault(2, 0) + freq.getOrDefault(1, 0)) / count;
	output.append(averageNet()).append(" ")
	    .append(playerAverage(0)).append("-").append(playerAverage(1))
	    .append(" ").append(freq.toString())
            .append(" Agent 1 win rate: ").append(agent1WinRate)
	    .append(" (").append(averageHands()).append(" hands/game)");
	return output.toString();
    }
}
