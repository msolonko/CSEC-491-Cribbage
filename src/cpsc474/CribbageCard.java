package cpsc474;

public class CribbageCard
{
    /**
     * The templated card wrapped in this card.
     */
    private Card<CardRank, Character> card;

    /**
     * Creates a card of the given rank and suit.
     *
     * @param r a cpsc474.CardRank, non-null
     * @param char a character
     */
    public CribbageCard(CardRank r, char s)
    {
	card = new Card<>(r, s);
    }

    /**
     * Creates a card to wrap the given templated card.
     *
     * @pram card a card, non-null
     */
    public CribbageCard(Card<CardRank, Character> card)
    {
	this.card = card;
    }

    /**
     * Returns the rank of this card.
     *
     * @return the rank of this card, non-null
     */
    public CardRank getRank()
    {
	return card.getRank();
    }

    /**
     * Returns the suit of this card.
     *
     * @return the suit of this card
     */
    public char getSuit()
    {
	return card.getSuit();
    }
    
    /**
     * Determines if this card equals the given other card.  Two cards
     * are equal if and only if their ranks and equal and their suits are
     * equal.
     *
     * @param other a pointer to a card, non-null
     * @return true if the two cards are equal, false otherwise
     */
    public boolean equals(CribbageCard other)
    {
	return card.equals(other.card);
    }

    /**
     * Returns the hash code of this card.
     *
     * @return the hash code of this card
     */
    public int hashCode()
    {
	return card.hashCode();
    }

    /**
     * Returns a printable representation of this card.
     *
     * @return a printable representation of this card.
     */
    public String toString()
    {
	return card.toString();
    }

    /**
     * Returns the templated card wrapped in this card.
     *
     * @return the templated card wrapped in this card
     */
    public Card<CardRank, Character> unwrap()
    {
	return card;
    }
}
