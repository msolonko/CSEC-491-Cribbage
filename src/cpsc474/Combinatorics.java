package cpsc474;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Combinatorics
{
    static Map<List<Integer>, Long> combinationsMemo = new HashMap<>();

    public static long combinations(int n, int k)
    {
	if (n < k)
	    {
		return 0;
	    }
	else if (n == k)
	    {
		return 1;
	    }
	else if (k == 0)
	    {
		return 1;
	    }
	else
	    {
		List<Integer> key = Arrays.asList(new Integer[] {n, k});
		if (combinationsMemo.containsKey(key))
		    {
			return combinationsMemo.get(key);
		    }
		else
		    {
			long value = combinations(n - 1, k) + combinations(n - 1, k - 1);
			combinationsMemo.put(key, value);
			return value;
		    }
	    }
    }

    public static void main(String[] args)
    {
	for (int a = 0; a + 1 < args.length; a += 2)
	    {
		System.out.println(Combinatorics.combinations(Integer.parseInt(args[a]), Integer.parseInt(args[a + 1])));
	    }
    }
}
