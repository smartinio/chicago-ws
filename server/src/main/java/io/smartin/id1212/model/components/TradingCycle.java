package io.smartin.id1212.model.components;

import java.util.*;

public class TradingCycle {
    private Map<Player,List<PlayingCard>> thrown = new HashMap<>();

    public boolean isFinished(int numPlayers) {
        return thrown.size() == numPlayers;
    }

    public void addPlayerThrow(Player player, List<PlayingCard> cards) {
        thrown.put(player, cards);
    }

    public List<PlayingCard> getCards() {
        List<PlayingCard> cards = new ArrayList<>();
        for (List<PlayingCard> playingCards : thrown.values()) {
            cards.addAll(playingCards);
        }
        return cards;
    }
}
