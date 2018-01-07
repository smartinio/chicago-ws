package io.smartin.id1212.model.components.pokerhands;

import io.smartin.id1212.exceptions.game.HandsAreEqualException;
import io.smartin.id1212.model.components.comparators.SortByValue;
import io.smartin.id1212.model.components.Hand;
import io.smartin.id1212.model.components.PlayingCard;
import io.smartin.id1212.model.components.pokerhands.abstracts.PokerHand;

import java.util.ArrayList;
import java.util.List;

public class Nothing extends PokerHand {
    private Hand hand;

    public Nothing(Hand hand) {
        this.hand = hand;
    }

    public Hand getHand() {
        return hand;
    }

    @Override
    public Hand.HandType getType() {
        return Hand.HandType.NOTHING;
    }

    @Override
    public boolean beats(PokerHand pokerHand) throws HandsAreEqualException {
        if (pokerHand instanceof Nothing) {
            Nothing nothing = (Nothing) pokerHand;
            List<PlayingCard> myCards = new ArrayList<>(hand.getCards());
            List<PlayingCard> otherCards = new ArrayList<>(nothing.getHand().getCards());
            myCards.sort(new SortByValue());
            otherCards.sort(new SortByValue());
            for (int i = 0; i < myCards.size(); i++) {
                if (myCards.get(i).getValue().ordinal() > otherCards.get(i).getValue().ordinal()) return true;
                if (myCards.get(i).getValue().ordinal() < otherCards.get(i).getValue().ordinal()) return false;
            }
            throw new HandsAreEqualException();
        }
        return beats(pokerHand.getType());
    }
}
