package cpsc474;

import java.util.Arrays;

public class RandomThrower implements KeepPolicy
{
    public CribbageHand[] keep(CribbageHand cards, int[] scores, boolean amDealer)
    {
	// select two indices uniformly randomly
	int index1 = (int)(Math.random() * cards.size());
	int index2 = (int)(Math.random() * (cards.size() - 1));
	if (index2 >= index1)
	    {
		index2++;
	    }

	// sort the indices
	Integer[] indices = new Integer[] {index1, index2};
	Arrays.sort(indices);

	// split the hand
	return cards.split(Arrays.asList(indices));
    }
}

