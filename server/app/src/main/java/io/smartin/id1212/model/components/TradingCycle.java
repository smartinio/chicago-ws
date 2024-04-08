package io.smartin.id1212.model.components;

import java.util.*;

public class TradingCycle {
    private Map<Player,Set<PlayingCard>> thrown = new HashMap<>();

    public boolean isFinished(List<Player> tradeEligiblePlayers) {
        return tradeEligiblePlayers.stream().allMatch(player -> thrown.containsKey(player));
    }

    public void addPlayerThrow(Player player, Set<PlayingCard> cards) {
        thrown.put(player, cards);
    }

    public List<PlayingCard> getCards() {
        List<PlayingCard> cards = new ArrayList<>();
        for (Set<PlayingCard> playingCards : thrown.values()) {
            cards.addAll(playingCards);
        }
        return cards;
    }
}
