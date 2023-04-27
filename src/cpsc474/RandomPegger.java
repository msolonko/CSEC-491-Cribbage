package cpsc474;

public class RandomPegger implements PegPolicy
{
    @Override
    public CribbageCard peg(CribbageHand cards, PeggingHistory hist, int[] scores, boolean amDealer)
    {
	int legalPlays = 0;
	CribbageCard play = null;
	for (CribbageCard c : cards)
	    {
		if (hist.isLegal(c, amDealer ? 0 : 1))
		    {
			// retaining a card with probability 1/current-count
			// retains each with equal probability
			legalPlays++;
			if (play == null || Math.random() < 1.0 / legalPlays)
			    {
				play = c;
			    }
		    }
	    }
	return play;
    }
}
