package io.smartin.id1212.model.components.pokerhands.abstracts;

import io.smartin.id1212.exceptions.game.HandsAreEqualException;
import io.smartin.id1212.model.components.Hand;

public abstract class PokerHand {
    private Hand.HandType type;
    public Hand.HandType getType() {
        return type;
    }
    abstract public boolean beats(PokerHand pokerHand) throws HandsAreEqualException;
    protected boolean beats(Hand.HandType handType) {
        return getType().ordinal() > handType.ordinal();
    }
}
