package cpsc474;

import java.util.List;

/**
 * A type of card ranks.
 *
 * @param T the type of the individual ranks
 */
public interface SuitType<T>
{
    /**
     * Returns a list of all the suits in this type.
     *
     * @return a list of all the suits in this type
     */
    public List<T> allSuits();
}
