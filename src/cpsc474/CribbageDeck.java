package cpsc474;

import java.util.List;
import java.util.ArrayList;

public class CribbageDeck
{
    private Deck<CardRank, Character, OrderedRanks, CharacterSuits> deck;

    /**
     * Creates a deck of cards containing one card of each combination of
     * rank (A through K) and suit (C, D, H, or S).
     */
    public CribbageDeck()
    {
	OrderedRanks cardRanks = new OrderedRanks(new String[] {"A", "2", "3", "4", "5", "6", "7", "8", "9", "T,10", "J", "Q", "K"});
	CharacterSuits cardSuits = new CharacterSuits("SHDC");
	deck = new Deck<>(cardRanks, cardSuits, 1);
    }

    /**
     * Shuffles this deck.
     */
    public void shuffle()
    {
	deck.shuffle();
    }
   
    /**
     * Returns the number of cards in this deck.
     *
     * @return the number of cards in this deck
     */
    public int size()
    {
	return deck.size();
    }

    /**
     * Removes and returns the next n cards in this deck.  The relative
     * order of the remaining cards remains the same.
     *
     * @param n a nonnegative integer
     * @return a non-null list of non-null cards 
     */
    public List<CribbageCard> deal(int n)
    {
	List<Card<CardRank, Character>> raw = deck.deal(n);

	// wrap cpsc474.Card<R, S> objects into cpsc474.CribbageCard
	List<CribbageCard> wrapped = new ArrayList<>();
	for (Card<CardRank, Character> c : raw)
	    {
		wrapped.add(new CribbageCard(c));
	    }
	return wrapped;
    }

    /**
     * Returns the next n cards that would be dealt from this deck.
     * The cards are not removed and the order of the cards in the deck
     * is not changed.
     *
     * @param n a nonnegative integer
     * @return a non-null list of non-null cards 
     */
    public List<CribbageCard> peek(int n)
    {
	List<Card<CardRank, Character>> raw = deck.peek(n);
	
	// wrap cpsc474.Card<R, S> objects into cpsc474.CribbageCard
	List<CribbageCard> wrapped = new ArrayList<>();
	for (Card<CardRank, Character> c : raw)
	    {
		wrapped.add(new CribbageCard(c));
	    }
	return wrapped;
    }

    /**
     * Removes the given cards from this deck.  If there is a card
     * to remove that isn't present in this deck, then the effect is
     * the same as if that card had not been included in the list to
     * remove.  If there are multiple occurrences of a given card
     * in the list to remove, then the corresponding number of occurrences
     * of that card in this deck are removed.  The order of the
     * remaining cards is not specified.
     *  
     * @param cards an iterable over the cards to remove
     */
    public void remove(Iterable<CribbageCard> cards)
    {
	// unwrap cards
	List<Card<CardRank, Character>> raw = new ArrayList<>();
	for (CribbageCard c : cards)
	    {
		raw.add(c.unwrap());
	    }
	
	deck.remove(raw);
    }
}

