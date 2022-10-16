package io.smartin.id1212.model.components.pokerhands;

import io.smartin.id1212.model.components.Hand;
import io.smartin.id1212.model.components.PlayingCard;
import io.smartin.id1212.model.components.pokerhands.abstracts.NOfAKind;

public class FourOfAKind extends NOfAKind {
    @Override
    public Hand.HandType getType() {
        return Hand.HandType.FOUR_OF_A_KIND;
    }

    public FourOfAKind(PlayingCard.Value of) {
        super(of);
    }
}
