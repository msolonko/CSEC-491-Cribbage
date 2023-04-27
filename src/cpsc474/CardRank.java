package cpsc474;

/**
 * A rank of cards.
 */

public class CardRank implements Comparable<CardRank>
{
    private int ordinal;
    private String name;

    public CardRank(int o, String n)
    {
	this.ordinal = o;
	this.name = n;
    }

    /**
     * Returns a unique identifying integer for this rank.  If two
     * cpsc474.CardRank objects are equal according to the equals method,
     * they will return the same value from this method.
     *
     * @return a unique identifying integer for this rank
     */
    public int intValue()
    {
	return ordinal;
    }

    public String getName()
    {
	return name;
    }
    
    public boolean equals(Object o)
    {
	return (o != null && o instanceof CardRank && ((CardRank)o).ordinal == ordinal);
    }

    public int compareTo(CardRank o)
    {
	return ordinal - ((CardRank)o).ordinal;
    }
    
    public int hashCode()
    {
	return ordinal;
    }
    
    public String toString()
    {
	return name;
    }
}
