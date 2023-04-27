package cpsc474;

public interface PegPolicy
{
    CribbageCard peg(CribbageHand cards, PeggingHistory hist, int[] scores, boolean amDealer);
}

