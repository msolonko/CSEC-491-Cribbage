package cpsc474;

import cpsc474.CribbageHand;

public interface KeepPolicy
{
    CribbageHand[] keep(CribbageHand cards, int[] scores, boolean amDealer);
}

