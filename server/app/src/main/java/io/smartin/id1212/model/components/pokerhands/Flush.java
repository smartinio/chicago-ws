package io.smartin.id1212.model.components.pokerhands;

import io.smartin.id1212.exceptions.game.HandsAreEqualException;
import io.smartin.id1212.model.components.comparators.SortByValue;
import io.smartin.id1212.model.components.Hand;
import io.smartin.id1212.model.components.PlayingCard;
import io.smartin.id1212.model.components.pokerhands.abstracts.PokerHand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Flush extends PokerHand {
    private Hand hand;

    public Flush(Hand hand) {
        this.hand = hand;
    }

    private Hand getHand() {
        return hand;
    }

    @Override
    public Hand.HandType getType() {
        return Hand.HandType.FLUSH;
    }

    @Override
    public boolean beats(PokerHand pokerHand) throws HandsAreEqualException {
        if (pokerHand instanceof Flush) {
            Flush other = (Flush) pokerHand;
            List<PlayingCard> myCards = new ArrayList<>(hand.getCards());
            List<PlayingCard> otherCards = new ArrayList<>(other.getHand().getCards());
            Collections.sort(myCards, new SortByValue());
            Collections.sort(otherCards, new SortByValue());
            for (int i = myCards.size() -1; i >= 0; i--) {
                if (!myCards.get(i).getValue().equals(otherCards.get(i).getValue())) {
                    return myCards.get(i).getValue().ordinal() > otherCards.get(i).getValue().ordinal();
                }
            }
            throw new HandsAreEqualException();
        }
        return beats(pokerHand.getType());
    }
}
