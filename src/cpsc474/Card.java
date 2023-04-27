package cpsc474;

/**
 * A card with ranks and suits of the given types.
 *
 * @param R the type of the rank
 * @param S the type of the suit
 */

public class Card<R, S>
{
    private R rank;
    private S suit;
    private int hash;

    /**
     * Creates a card with the given rank and suit.
     *
     * #param rank a rank
     * @param suit a suit
     */
    public Card(R rank, S suit)
    {
	this.rank = rank;
	this.suit = suit;
	this.hash = rank.hashCode() ^ suit.hashCode();
    }

    /**
     * Returns the rank of this card.
     *
     * @return the rank of this card
     */
    public R getRank()
    {
	return rank;
    }

    /**
     * Returns the suit of this card.
     *
     * @return the suit of this card
     */
    public S getSuit()
    {
	return suit;
    }

    /**
     * Determines if this card is equal to the given object.
     * Two cards are equal if their ranks and suits are equal.  Cards
     * are not equal to objects of other types.
     *
     * @param o an object, or null
     */
    public boolean equals(Object o)
    {
	if (o != null && o instanceof Card)
	    {
		Card other = (Card)o;
		return other.rank.equals(rank) && other.suit.equals(suit);
	    }
	return false;
    }

    /**
     * Returns the hash code of this card.
     *
     * @return the hash code of this card
     */
    public int hashCode()
    {
	return hash;
    }

    /**
     * Returns a printable representation of this card.
     *
     * @return a printable representation of this card
     */
    public String toString()
    {
	return rank.toString() + suit.toString();
    }
}

    
