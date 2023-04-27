package cpsc474;

import java.util.List;
import java.util.ArrayList;

public class IntegerRanks implements RankType<Integer>
{
    private int low;
    private int high;

    public IntegerRanks(int low, int high)
    {
	this.low = low;
	this.high = high;
    }

    public List<Integer> allRanks()
    {
	List<Integer> ranks = new ArrayList<Integer>();
	for (int r = low; r <= high; r++)
	    {
		ranks.add(r);
	    }
	return ranks;
    }
}

	
