package io.smartin.id1212.model.components;

import io.smartin.id1212.exceptions.game.OutOfCardsException;

import java.util.*;

import static io.smartin.id1212.config.Strings.EMPTY_DECK;

public class CardDeck {
    private final List<PlayingCard> shuffledCards = new ArrayList<>();

    public CardDeck () {
        for (PlayingCard.Suit suit : PlayingCard.Suit.values()) {
            for (PlayingCard.Value value : PlayingCard.Value.values()) {
                shuffledCards.add(new PlayingCard(suit, value));
            }
        }
        var isTest = "test".equals(System.getProperty("env"));
        Random random = isTest ? new Random(1337) : new Random();
        Collections.shuffle(shuffledCards, random);
    }

    public Set<PlayingCard> getCards() {
        return new HashSet<>(shuffledCards);
    }

    public void addCards(Collection<PlayingCard> thrown) {
        shuffledCards.addAll(thrown);
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
        synchronized (shuffledCards) {
            if (shuffledCards.size() < amount) {
                throw new OutOfCardsException(EMPTY_DECK);
            }
            Set<PlayingCard> cards = new HashSet<>();
            for (int i=0; i<amount; i++) {
                cards.add(shuffledCards.remove(0));
            }
            return cards;
        }
    }
}
