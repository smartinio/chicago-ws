package io.smartin.id1212.model.managers;

import io.smartin.id1212.exceptions.game.OutOfCardsException;
import io.smartin.id1212.model.components.*;
import io.smartin.id1212.model.managers.ScoreManager.BestHandResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TradingManager {
    private Round round;
    private List<TradingCycle> tradingCycles = new ArrayList<>();
    private int MAX_TRADES_PER_ROUND;

    public TradingManager(Round round) {
        this.round = round;
        this.tradingCycles.add(new TradingCycle());
        this.MAX_TRADES_PER_ROUND = round.getGame().getRules().numTrades;
    }

    public void throwCards(Player player, Set<PlayingCard> cards) throws OutOfCardsException {
        synchronized (round) {
            if (cards.size() > 0) {
                CardDeck deck = round.getDeck();
                player.removeCards(cards);
                deck.addCards(cards);
                currentCycle().addPlayerThrow(player, cards);
            } else {
                currentCycle().addPlayerThrow(player, new HashSet<>());
            }
        }
    }

    public void drawCards(Player player, Set<PlayingCard> cards) throws OutOfCardsException {
        CardDeck deck = round.getDeck();
        player.giveCards(deck.draw(cards.size()));
    }

    public List<BestHandResult> completeTrade() {
        List<BestHandResult> result = new ArrayList<>();
        List<Player> tradeEligiblePlayers = round.getGame().getTradeEligiblePlayers();

        if (currentCycle().isFinished(tradeEligiblePlayers)) {
            if (maxTradingCyclesReached()) {
                round.endTradingPhase();
            } else {
                result = round.getGame().getScoreManager().givePointsForBestHand();
                tradingCycles.add(new TradingCycle());
            }
        }

        return result;
    }

    public boolean maxTradingCyclesReached() {
        return tradingCycles.size() == MAX_TRADES_PER_ROUND;
    }

    private TradingCycle currentCycle() {
        return tradingCycles.get(tradingCycles.size() - 1);
    }
}
