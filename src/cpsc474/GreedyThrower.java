package cpsc474;

import java.util.List;
import java.util.ArrayList;

public class GreedyThrower implements KeepPolicy
{
    private CribbageGame game;
    private List<List<Integer>> possibleThrows;
    
    public GreedyThrower(CribbageGame game)
    {
	this.game = game;
	this.possibleThrows = new ArrayList<>();
	
	List<Integer> indices = new ArrayList<>();
	for (int i = 0; i < game.cardsDealt(); i++)
	    {
		indices.add(i);
	    }

	// make list of list of combinations of 2 indices for possible
	// indices of cards to throw
	IterTools.combinations(indices, game.cardsDealt() - game.cardsToKeep()).forEach(possibleThrows::add);
    }

    @Override
    public CribbageHand[] keep(CribbageHand cards, int[] scores, boolean amDealer)
    {
	CribbageHand[] bestSplit = null;
	int bestNet = Integer.MIN_VALUE;
	int ties = 0;
	for (List<Integer> throwIndices : possibleThrows)
	    {
		CribbageHand[] currSplit = cards.split(throwIndices);
		int netPoints = game.score(currSplit[0], null, false)[0] + (amDealer ? 1 : -1) * game.score(currSplit[1], null, true)[0];
		if (netPoints > bestNet)
		    {
			bestSplit = currSplit;
			bestNet = netPoints;
			ties = 0;
		    }
		else if (netPoints == bestNet)
		    {
			// retain ties with probability 1/#ties for uniform
			// random tiebreaking
			ties++;
			if (Math.random() < 1.0 / ties)
			    {
				bestSplit = currSplit;
			    }
		    }
	    }
	
	return bestSplit;
    }
}
