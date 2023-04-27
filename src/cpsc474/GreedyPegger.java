package cpsc474;

public class GreedyPegger implements PegPolicy
{
    @Override
    public CribbageCard peg(CribbageHand cards, PeggingHistory hist, int[] scores, boolean amDealer)
    {
	// maximize score earned over all cards played, breaking ties
	// uniformly randomly
	int ties = 0;
	CribbageCard bestPlay = null;
	int bestScore = Integer.MIN_VALUE;
	for (CribbageCard c : cards)
	    {
		if (hist.isLegal(c, amDealer ? 0 : 1))
		    {
			int[] score = hist.score(c, amDealer ? 0 : 1);
			if (score[0] > bestScore)
			    {
				bestScore = score[0];
				bestPlay = c;
				ties = 0;
			    }
			else if (score[0] == bestScore)
			    {
				ties++;
				if (Math.random() < 1.0 / ties)
				    {
					bestPlay = c;
				    }
			    }
		    }
	    }
	return bestPlay;
    }
}
