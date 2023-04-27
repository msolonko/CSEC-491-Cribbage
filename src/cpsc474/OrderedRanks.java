package cpsc474;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class OrderedRanks implements RankType<CardRank>
{
    private List<CardRank> ranks;
    
    private Map<String, String> aliases;
    
    /**
     * Creates an ordering of ranks.  Ranks are non-null strings with no
     * commas.  If there is more than one string representation of the same
     * rank, then the list of aliases can be given as a comma-separated
     * list with the default first, as "T,10".
     *
     * @param ranks an array containing non-null strings giving the ranks
     * or lists of aliases of ranks; ranks are listed from smallest to largest
     */
    public OrderedRanks(String[] ranks)
    {
	this.ranks = new ArrayList<>();
	this.aliases = new HashMap<>();
	
	for (int i = 0; i < ranks.length; i++)
	    {
		String[] rankAliases = ranks[i].split(",");
		this.ranks.add(new CardRank(i, rankAliases[0]));
		for (int j = 1; j < rankAliases.length; j++)
		    {
			aliases.put(rankAliases[j], rankAliases[0]);
		    }
	    }
    }

    public List<CardRank> allRanks()
    {
	return Collections.unmodifiableList(ranks);
    }

    public CardRank parseRank(String rank)
    {
	if (aliases.containsKey(rank))
	    {
		rank = aliases.get(rank);
	    }
	
	for (CardRank r : ranks)
	    {
		if (r.getName().equals(rank))
		    {
			return r;
		    }
	    }
	return null;
    }
}

	
