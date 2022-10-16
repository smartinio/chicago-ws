package io.smartin.id1212.model.components.pokerhands;

import io.smartin.id1212.exceptions.game.HandsAreEqualException;
import io.smartin.id1212.model.components.Hand;
import io.smartin.id1212.model.components.PlayingCard;
import io.smartin.id1212.model.components.pokerhands.abstracts.PokerHand;

public class FullHouse extends PokerHand {
    private PlayingCard.Value highest;

    public FullHouse(PlayingCard.Value highest) {
        this.highest = highest;
    }

    private PlayingCard.Value getHighest() {
        return highest;
    }

    @Override
    public Hand.HandType getType() {
        return Hand.HandType.FULL_HOUSE;
    }

    @Override
    public boolean beats(PokerHand pokerHand) throws HandsAreEqualException {
        if (pokerHand instanceof FullHouse) {
            FullHouse other = (FullHouse) pokerHand;
            if (highest.ordinal() > other.getHighest().ordinal()) return true;
            if (highest.ordinal() < other.getHighest().ordinal()) return false;
            throw new HandsAreEqualException();
        }
        return beats(pokerHand.getType());
    }
}
