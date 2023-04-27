package cpsc474;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class CribbageHand implements Iterable<CribbageCard>
{
    public List<CribbageCard> cards;

    /**
     * Creates a hand from the given cards.
     *
     * @param c a non-null list of non-null CribbageCards
     */
    public CribbageHand(List<CribbageCard> c)
    {
	cards = new ArrayList<>(c);
    }

    /**
     * Creates a new cpsc474.CribbageHand containing the union of the given
     * two hands.  If there are duplicates of a card, then the new hand
     * will contain the corresponding number of copies of that card.
     *
     * @param h1 a cpsc474.CribbageHand, non-null
     * @param h2 a cpsc474.CribbageHand, non-null
     */
    public CribbageHand(CribbageHand h1, CribbageHand h2)
    {
	cards = new ArrayList<>(h1.cards);
	cards.addAll(h2.cards);
    }

    /**
     * Returns the number of cards in this hand.
     *
     * @return the number of cards in this hand
     */
    public int size()
    {
	return cards.size();
    }

    /**
     * Returns an iterator over cards in this hand.
     *
     * @return an iterator over cards in this hand
     */
    public Iterator<CribbageCard> iterator()
    {
	return Collections.unmodifiableList(cards).iterator();
    }

    /**
     * Returns the pair of CribbageHands obtained by creating one hand
     * from the cards not at the indices in the given list and the other from
     * the cards at those indices.
     *
     * @param indices a list of integer indices of cards in this hand ordered
     * from smallest to largest index
     * @return a 2-element array containing the hand of cards retained in
     * index 0 and the hand of cards removed in index 1
     */
    public CribbageHand[] split(List<Integer> indices)
    {
	List<CribbageCard> keep = new ArrayList<>();
	List<CribbageCard> remove = new ArrayList<>();

	int curr = 0;
	for (Integer r: indices)
	    {
		for (int i = curr; i < r && i < cards.size(); i++)
		    {
			keep.add(cards.get(i));
		    }
		remove.add(cards.get(r));
		curr = r + 1;
	    }
	
	for (int i = curr; i < cards.size(); i++)
	    {
		keep.add(cards.get(i));
	    }
	
	return new CribbageHand[] {new CribbageHand(keep), new CribbageHand(remove)};
    }

    /**
     * Determines if the given split is a valid partition of this hand.
     * A partition is valid if each card in this hand is present exactly once
     * in exactly one part of the partition.
     *
     * @param split a array of non-null CribbageHands, non-null
     */
    public boolean isLegalSplit(CribbageHand[] split)
    {
	// count cards in this hand
	Map<String, Integer> cardCount = new HashMap<>();
	for (CribbageCard c : this)
	    {
			String cStr = c.toString();
		if (!cardCount.containsKey(cStr))
		    {
			cardCount.put(cStr, 1);
		    }
		else
		    {
			cardCount.put(cStr, cardCount.get(cStr) + 1);
		    }
	    }


	int partitionSize = 0;
	// match cards in parts of the partition with cards in hand
	for (CribbageHand part : split)
	    {
		partitionSize += part.size();
		for (CribbageCard c : part)
		    {
				String cStr = c.toString();
			if (!cardCount.containsKey(cStr) || cardCount.get(cStr) == 0)
			    {
				return false;
			    }
			cardCount.put(cStr, cardCount.get(cStr) - 1);
		    }
	    }


	// if no mismatches yet and sizes are good, everything is OK
	return partitionSize == size();
    }

    /**
     * Returns the cribbage hand that results from removing the given
     * card from this hand.  If the card is not present in this hand
     * then the return value is null.
     *
     * @param card a card, non-null
     */
    public CribbageHand remove(CribbageCard card)
    {
	List<CribbageCard> result = new ArrayList<>();
	for (CribbageCard curr : this)
	    {
		if (card == null || !card.equals(curr))
		    {
			result.add(curr);
		    }
		else
		    {
			card = null;
		    }
	    }
	if (card == null)
	    {
		return new CribbageHand(result);
	    }
	else
	    {
		// didn't remove card (it was not present)
		return null;
	    }
    }
    
    /**
     * Returns a printable representation of this hand.  That representation
     * will contain the representation of the cards in this hand.
     *
     * @return a printable representation of this hand
     */
    public String toString()
    {
	return cards.toString();
    }
}
