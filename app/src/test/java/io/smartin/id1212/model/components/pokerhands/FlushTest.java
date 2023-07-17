package io.smartin.id1212.model.components.pokerhands;

import io.smartin.id1212.exceptions.game.HandsAreEqualException;
import io.smartin.id1212.model.components.*;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static io.smartin.id1212.model.components.PlayingCard.Suit.*;
import static io.smartin.id1212.model.components.PlayingCard.Value.*;
import static org.junit.jupiter.api.Assertions.*;

class FlushTest {

    @Test
    void correctlyComparesToAnotherFlush() throws HandsAreEqualException {
        Set<PlayingCard> strongerCards = new HashSet<>();
        strongerCards.add(new PlayingCard(SPADES, ACE));
        strongerCards.add(new PlayingCard(SPADES, KING));
        strongerCards.add(new PlayingCard(SPADES, TEN));
        strongerCards.add(new PlayingCard(SPADES, FIVE));
        strongerCards.add(new PlayingCard(SPADES, THREE));

        Hand strongerHand = new Hand(strongerCards);
        Flush strongerFlush = new Flush(strongerHand);

        Set<PlayingCard> weakerCards = new HashSet<>();
        weakerCards.add(new PlayingCard(HEARTS, ACE));
        weakerCards.add(new PlayingCard(HEARTS, QUEEN));
        weakerCards.add(new PlayingCard(HEARTS, EIGHT));
        weakerCards.add(new PlayingCard(HEARTS, SEVEN));
        weakerCards.add(new PlayingCard(HEARTS, SIX));

        Hand weakerHand = new Hand(weakerCards);
        Flush weakerFlush = new Flush(weakerHand);

        assertTrue(strongerFlush.beats(weakerFlush));
    }


    @Test
    void correctlyComparesToACoolerFlush() throws HandsAreEqualException {
        Set<PlayingCard> strongerCards = new HashSet<>();
        strongerCards.add(new PlayingCard(SPADES, ACE));
        strongerCards.add(new PlayingCard(SPADES, KING));
        strongerCards.add(new PlayingCard(SPADES, TEN));
        strongerCards.add(new PlayingCard(SPADES, FIVE));
        strongerCards.add(new PlayingCard(SPADES, THREE));

        Hand strongerHand = new Hand(strongerCards);
        Flush strongerFlush = new Flush(strongerHand);

        Set<PlayingCard> weakerCards = new HashSet<>();
        weakerCards.add(new PlayingCard(HEARTS, ACE));
        weakerCards.add(new PlayingCard(HEARTS, KING));
        weakerCards.add(new PlayingCard(HEARTS, TEN));
        weakerCards.add(new PlayingCard(HEARTS, FIVE));
        weakerCards.add(new PlayingCard(HEARTS, TWO));

        Hand weakerHand = new Hand(weakerCards);
        Flush weakerFlush = new Flush(weakerHand);

        assertTrue(strongerFlush.beats(weakerFlush));
    }

    @Test
    void correctlyComparesWhenCardsArePlayed() throws HandsAreEqualException {
        Set<PlayingCard> strongerCards = new HashSet<>();
        strongerCards.add(new PlayingCard(DIAMONDS, ACE));
        strongerCards.add(new PlayingCard(DIAMONDS, QUEEN));
        strongerCards.add(new PlayingCard(DIAMONDS, SEVEN));
        strongerCards.add(new PlayingCard(DIAMONDS, SIX));
        strongerCards.add(new PlayingCard(DIAMONDS, THREE));

        Hand strongerHand = new Hand(strongerCards);
        strongerHand.moveAllToPlayed(true);

        Set<PlayingCard> weakerCards = new HashSet<>();
        weakerCards.add(new PlayingCard(CLUBS, NINE));
        weakerCards.add(new PlayingCard(CLUBS, EIGHT));
        weakerCards.add(new PlayingCard(CLUBS, SIX));
        weakerCards.add(new PlayingCard(CLUBS, FOUR));
        weakerCards.add(new PlayingCard(CLUBS, TWO));

        Hand weakerHand = new Hand(weakerCards);
        weakerHand.moveAllToPlayed(true);

        assertTrue(strongerHand.beats(weakerHand));
    }
}
