package cpsc474;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * A cpsc474.Deck of cards of the given types of rank and suit.
 *
 * @param R an enumerable type of ranks of cards
 * @param S an enumerable type of suits of cards
 */

public class Deck<R, S, RT extends RankType<R>, ST extends SuitType<S>>
{
    /**
     * The cards in this deck.
     */
    private List<Card<R, S>> cards;
    
    /**
     * Creates a deck containing the given number of occurrences of
     * each possible combination of rank and suit.
     *
     * @param n a nonnegative integer
     */
    public Deck(RankType<R> ranks, SuitType<S> suits, int n)
    {
	cards = new ArrayList<Card<R, S>>();
	for (R r: ranks.allRanks())
	    {
		for (S s: suits.allSuits())
		    {
			for (int i = 0; i < n; i++)
			    {
				cards.add(new Card<R, S>(r, s));
			    }
		    }
	    }
    }


    /**
     * Shuffles this deck.
     */
    public void shuffle()
    {
	Collections.shuffle(cards);
    }
   
    /**
     * Returns the number of cards in this deck.
     *
     * @return the number of cards in this deck
     */
    public int size()
    {
	return cards.size();
    }

    /**
     * Removes and returns the next n cards in this deck.
     *
     * @param n a nonnegative integer
     */
    public List<Card<R, S>> deal(int n)
    {
	List<Card<R, S>> result = new ArrayList<Card<R, S>>(cards.subList(cards.size() - n, cards.size()));
	for (int i = 0; i < n; i++)
	    {
		cards.remove(cards.size() - 1);
	    }
	return result;
    }

    /**
     * Returns the next n cards in this deck.  The cards are not removed
     *
     * @param n a nonnegative integer
     * @return a list of the next cards
     */
    public List<Card<R, S>> peek(int n)
    {
	return new ArrayList<Card<R, S>>(cards.subList(cards.size() - n, cards.size()));
    }

    /**
     *  Removes the given cards from this deck.  If there is a card
     *  to remove that isn't present in this deck, then the effect is
     *  the same as if that card had not been included in the list to
     *  remove.  If there are multiple occurrences of a given card
     *  in the list to remove, then the corresponding number of occurrences
     *  of that card in this deck are removed.
     *  
     *  @param cards an iterable over the cards to remove
     */
    public void remove(Iterable<Card<R, S>> cards)
    {
	// count occurrences of cards to remove
	Map<Card<R, S>, Integer> counts = new HashMap<>();
	for (Card<R, S> c: cards)
	    {
		if (!counts.containsKey(c))
		    {
			counts.put(c, 0);
		    }
		counts.put(c, counts.get(c) + 1);
	    }

	// go through cards
	List<Card<R, S>> remaining = new ArrayList<Card<R, S>>();

	// TODO changed cards to this.cards
	for (Card<R, S> c : this.cards)
	    {
		if (counts.containsKey(c) && counts.get(c) > 0)
		    {
			counts.put(c, counts.get(c) - 1);
		    }
		else
		    {
			remaining.add(c);
		    }
	    }
	// TODO changed this from cards = remaining;
	this.cards = remaining;
    }

    public static void main(String[] args)
    {
	Deck<CardRank, Character, OrderedRanks, CharacterSuits> deck = new Deck<>(new OrderedRanks(new String[] {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"}), new CharacterSuits("SHDC"), 1);
	deck.shuffle();
	System.out.println(deck.deal(6));
    }
}
