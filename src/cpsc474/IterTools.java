package cpsc474;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public class IterTools
{
    public static <T> Iterable<List<T>> combinations(Iterable<T> all, int n)
    {
	ArrayList<T> items = new ArrayList<>();
	for (T item : all)
	    {
		items.add(item);
	    }
	return new Combinations<T>(items, n);
    }

    private static class Combinations<T> implements Iterable<List<T>>
    {
	private List<T> items;
	private int size;

	/**
	 * param n a positive integer
	 */
	// TO DO: allow n = 0
	public Combinations(List<T> items, int n)
	{
	    this.items = items;
	    this.size = n;
	}

	public Iterator<List<T>> iterator()
	{
	    return new CombinationsIterator();
	}

	private class CombinationsIterator implements Iterator<List<T>>
	{
	    private int[] curr;

	    public CombinationsIterator()
	    {
		curr = new int[size];
		for (int i = 0; i < size; i++)
		    {
			curr[i] = i;
		    }
	    }

	    public boolean hasNext()
	    {
		return curr[0] != -1;
	    }
	    
	    public List<T> next()
	    {
		// make a list of the items at the current indices
		List<T> subset = new ArrayList<>();
		for (int i = 0; i < size; i++)
		    {
			subset.add(items.get(curr[i]));
		    }
		
		// advance the current indices
		// find the incrementable position
		int pos = size - 1;
		while (pos > 0 && curr[pos] == pos + (items.size() - size))
		    {
			pos--;
		    }
		if (curr[pos] == pos + (items.size() - size))
		    {
			// no incrementable position -- signal completion
			curr[0] = -1;
		    }
		else
		    {
			// increment given position
			curr[pos] += 1;
			// set later positions in sequence
			for (int p = pos + 1; p < size; p++)
			    {
				curr[p] = curr[p - 1] + 1;
			    }
		    }
		
		// return the list
		return subset;
	    }
	}
	
    }

    public static void main(String[] args)
    {
	List<String> a = Arrays.asList(args);
	for (int n = 2; n <= args.length; n++)
	    {
		for (List<String> subset : IterTools.combinations(a, n))
		    {
			System.out.println(subset);
		    }
	    }
    }
}
    
