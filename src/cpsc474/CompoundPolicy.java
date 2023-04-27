package cpsc474;

/**
 * A policy composed of a cpsc474.KeepPolicy and a cpsc474.PegPolicy.
 */

public class CompoundPolicy implements CribbagePolicy
{
    private KeepPolicy keep;
    private PegPolicy peg;
    
    public CompoundPolicy(KeepPolicy keep, PegPolicy peg)
    {
	this.keep = keep;
	this.peg = peg;
    }

    @Override
    public CribbageHand[] keep(CribbageHand cards, int[] scores, boolean amDealer)
    {
	return keep.keep(cards, scores, amDealer);
    }

    @Override
    public CribbageCard peg(CribbageHand cards, PeggingHistory hist, int[] scores, boolean amDealer)
    {
	return peg.peg(cards, hist, scores, amDealer);
    }
}
