package cpsc474;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class CharacterSuits implements SuitType<Character>
{
    private List<Character> suits;

    public CharacterSuits(String suits)
    {
	this.suits = new ArrayList<Character>();
	for (int i = 0; i < suits.length(); i++)
	    {
		this.suits.add(suits.charAt(i));
	    }
    }

    public List<Character> allSuits()
    {
	return Collections.unmodifiableList(suits);
    }

    public Character parseSuit(String suit)
    {
	for (Character s : suits)
	    {
		if (suit.length() == 1 && s.equals(suit.charAt(0)))
		    {
			return s;
		    }
	    }
	return null;
    }
}

	
