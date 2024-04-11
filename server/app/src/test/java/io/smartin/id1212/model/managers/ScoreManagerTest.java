package io.smartin.id1212.model.managers;

import io.smartin.id1212.model.components.*;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.smartin.id1212.model.components.PlayingCard.Suit.*;
import static io.smartin.id1212.model.components.PlayingCard.Value.*;
import static org.junit.jupiter.api.Assertions.*;

class ScoreManagerTest {

    @Test
    void correctlyGivesPointsToCompetingHands() {
        Player winningPlayer = new Player("1");
        winningPlayer.setName("Winner");
        Set<PlayingCard> strongerCards = new HashSet<>();
        strongerCards.add(new PlayingCard(DIAMONDS, ACE));
        strongerCards.add(new PlayingCard(DIAMONDS, QUEEN));
        strongerCards.add(new PlayingCard(DIAMONDS, SEVEN));
        strongerCards.add(new PlayingCard(DIAMONDS, SIX));
        strongerCards.add(new PlayingCard(DIAMONDS, THREE));
        winningPlayer.setHand(new Hand(strongerCards));
        winningPlayer.getHand().moveAllToPlayed(true);

        Player losingPlayer = new Player("2");
        winningPlayer.setName("Loser");
        Set<PlayingCard> weakerCards = new HashSet<>();
        weakerCards.add(new PlayingCard(CLUBS, NINE));
        weakerCards.add(new PlayingCard(CLUBS, EIGHT));
        weakerCards.add(new PlayingCard(CLUBS, SIX));
        weakerCards.add(new PlayingCard(CLUBS, FOUR));
        weakerCards.add(new PlayingCard(CLUBS, TWO));
        losingPlayer.setHand(new Hand(weakerCards));
        losingPlayer.getHand().moveAllToPlayed(true);

        ScoreManager manager = new ScoreManager(List.of(winningPlayer, losingPlayer));

        assertEquals(0, winningPlayer.getScore());
        assertEquals(0, losingPlayer.getScore());

        manager.givePointsForBestHand();

        assertEquals(5, winningPlayer.getScore());
        assertEquals(0, losingPlayer.getScore());
    }
}
