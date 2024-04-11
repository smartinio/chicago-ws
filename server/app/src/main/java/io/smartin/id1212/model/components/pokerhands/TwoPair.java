package io.smartin.id1212.model.components.pokerhands;

import io.smartin.id1212.exceptions.game.HandsAreEqualException;
import io.smartin.id1212.model.components.Hand;
import io.smartin.id1212.model.components.PlayingCard;
import io.smartin.id1212.model.components.pokerhands.abstracts.PokerHand;

import java.util.Map;

public class TwoPair extends PokerHand {
    private PlayingCard.Value highest;
    private PlayingCard.Value second;
    private PlayingCard.Value kicker;

    public TwoPair(Map<PlayingCard.Value, Integer> countMap) {
        for (Map.Entry<PlayingCard.Value, Integer> entry : countMap.entrySet()) {
            if (entry.getValue() == 1) kicker = entry.getKey();
            if (entry.getValue() == 2 && highest == null) highest = entry.getKey();
            if (entry.getValue() == 2 && highest != null) {
                if (entry.getKey().ordinal() > highest.ordinal()) {
                    second = highest;
                    highest = entry.getKey();
                } else {
                    second = entry.getKey();
                }
            }
        }
    }

    private PlayingCard.Value getHighest() {
        return highest;
    }

    private PlayingCard.Value getSecond() {
        return second;
    }

    private PlayingCard.Value getKicker() {
        return kicker;
    }

    @Override
    public Hand.HandType getType() {
        return Hand.HandType.TWO_PAIR;
    }

    @Override
    public boolean beats(PokerHand pokerHand) throws HandsAreEqualException {
        if (pokerHand instanceof TwoPair other) {
            if (other.getHighest().ordinal() > getHighest().ordinal()) return false;
            if (other.getHighest().ordinal() < getHighest().ordinal()) return true;
            if (other.getSecond().ordinal() > getSecond().ordinal()) return false;
            if (other.getSecond().ordinal() < getSecond().ordinal()) return true;
            if (other.getKicker().ordinal() > getKicker().ordinal()) return false;
            if (other.getKicker().ordinal() < getKicker().ordinal()) return true;
            throw new HandsAreEqualException();
        }
        return beats(pokerHand.getType());
    }

}
