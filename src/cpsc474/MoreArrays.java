package cpsc474;

public class MoreArrays
{
    /**
     * Because Arrays.stream(arr).max().getAsInt() is absurd.
     */
    public static int max(int[] arr)
    {
	int result = Integer.MIN_VALUE;
	for (int x : arr)
	    {
		result = Math.max(result, x);
	    }
	return result;
    }

    public static int min(int[] arr)
    {
	int result = Integer.MAX_VALUE;
	for (int x : arr)
	    {
		result = Math.min(result, x);
	    }
	return result;
    }

    public static int[] reverse(int[] arr)
    {
	int[] result = new int[arr.length];
	for (int i = 0; i < arr.length; i++)
	    {
		result[i] = arr[arr.length - 1 - i];
	    }
	return result;
    }
}
