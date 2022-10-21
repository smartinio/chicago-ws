package io.smartin.id1212.model.components;

import io.smartin.id1212.exceptions.game.OutOfCardsException;

import java.util.*;

import static io.smartin.id1212.config.Strings.EMPTY_DECK;

public class CardDeck {
    private Set<PlayingCard> cards = new HashSet<>();

    public CardDeck (boolean full) {
        if (!full) {
            return;
        }
        for (PlayingCard.Suit suit : PlayingCard.Suit.values()) {
            for (PlayingCard.Value value : PlayingCard.Value.values()) {
                cards.add(new PlayingCard(suit, value));
            }
        }
    }

    public Set<PlayingCard> getCards() {
        return cards;
    }

    public void addCards(Collection<PlayingCard> thrown) {
        cards.addAll(thrown);
    }

    public Set<PlayingCard> drawInitial() {
        try {
            return draw(5);
        } catch (OutOfCardsException e) {
            e.printStackTrace();
            System.exit(1);
        }
        // unreachable
        return null;
    }

    public Set<PlayingCard> draw(int amount) throws OutOfCardsException {
        synchronized (cards) {
            if (cards.size() == 0)
            throw new OutOfCardsException(EMPTY_DECK);
            Set<PlayingCard> playingCards = new HashSet<>();
            Random random = new Random();
            for (int i = 0; i < amount; i++) {
                int n = random.nextInt(cards.size());
                PlayingCard toRemove = null;
                int j = 0;
                for (PlayingCard card : cards) {
                    if (j == n) {
                        playingCards.add(card);
                        toRemove = card;
                        break;
                    }
                    j++;
                }
                cards.remove(toRemove);
            }
            return playingCards;
        }
    }

    public int size() {
        return cards.size();
    }
}
