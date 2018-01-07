package io.smartin.id1212.model.components.pokerhands.abstracts;

import io.smartin.id1212.model.components.Hand;
import io.smartin.id1212.model.components.PlayingCard;

public abstract class NOfAKind extends PokerHand {
    private PlayingCard.Value of;
    private Hand.HandType type;

    @Override
    public Hand.HandType getType() {
        return type;
    }

    protected NOfAKind(PlayingCard.Value of) {
        this.of = of;
    }

    private PlayingCard.Value of() {
        return of;
    }

    @Override
    public boolean beats(PokerHand other) {
        if (this.getClass().equals(other.getClass())) {
            NOfAKind n = (NOfAKind) other;
            return of.ordinal() > n.of().ordinal();
        }
        return type.ordinal() > other.getType().ordinal();
    }
}
