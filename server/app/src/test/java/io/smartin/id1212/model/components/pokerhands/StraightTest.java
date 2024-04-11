package io.smartin.id1212.model.components.pokerhands;

import io.smartin.id1212.model.components.Hand;
import io.smartin.id1212.model.components.PlayingCard;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static io.smartin.id1212.model.components.PlayingCard.Suit.*;
import static io.smartin.id1212.model.components.PlayingCard.Value.*;
import static org.junit.jupiter.api.Assertions.*;

class StraightTest {
    @Test
    void correctlyDeterminesStraight() {
        Set<PlayingCard> straightCards = new HashSet<>();
        straightCards.add(new PlayingCard(SPADES, JACK));
        straightCards.add(new PlayingCard(CLUBS, TEN));
        straightCards.add(new PlayingCard(DIAMONDS, NINE));
        straightCards.add(new PlayingCard(HEARTS, EIGHT));
        straightCards.add(new PlayingCard(HEARTS, SEVEN));

        Hand straightHand = new Hand(straightCards);

        assertEquals(straightHand.getPokerHand().getClass(), Straight.class);
    }

    @Test
    void correctlyDeterminesStraightWithHighAce() {
        Set<PlayingCard> straightCards = new HashSet<>();
        straightCards.add(new PlayingCard(SPADES, ACE));
        straightCards.add(new PlayingCard(CLUBS, KING));
        straightCards.add(new PlayingCard(DIAMONDS, QUEEN));
        straightCards.add(new PlayingCard(HEARTS, JACK));
        straightCards.add(new PlayingCard(HEARTS, TEN));

        Hand straightHand = new Hand(straightCards);

        assertEquals(straightHand.getPokerHand().getClass(), Straight.class);
    }

    @Test
    void correctlyDeterminesStraightWithLowAce() {
        Set<PlayingCard> straightCards = new HashSet<>();
        straightCards.add(new PlayingCard(SPADES, ACE));
        straightCards.add(new PlayingCard(CLUBS, FIVE));
        straightCards.add(new PlayingCard(DIAMONDS, FOUR));
        straightCards.add(new PlayingCard(HEARTS, THREE));
        straightCards.add(new PlayingCard(HEARTS, TWO));

        Hand straightHand = new Hand(straightCards);

        assertEquals(straightHand.getPokerHand().getClass(), Straight.class);
    }
}
