package io.smartin.id1212.model.managers;

import io.smartin.id1212.exceptions.game.OutOfCardsException;
import io.smartin.id1212.model.components.*;
import io.smartin.id1212.model.managers.ScoreManager.BestHandResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.smartin.id1212.config.Rules.MAX_TRADES_PER_ROUND;

public class TradingManager {
    private Round round;
    private List<TradingCycle> tradingCycles = new ArrayList<>();

    public TradingManager(Round round) {
        this.round = round;
        this.tradingCycles.add(new TradingCycle());
    }

    public List<BestHandResult> handle(Player player, Set<PlayingCard> cards) throws OutOfCardsException {
        int maxThrows = round.getGame().getPlayers().size();
        List<BestHandResult> result = new ArrayList<>();

        synchronized (round) {
            if (cards.size() > 0) {
                CardDeck deck = round.getDeck();
                player.removeCards(cards);
                deck.addCards(cards);
                player.giveCards(deck.draw(cards.size()));
                currentCycle().addPlayerThrow(player, cards);
            } else {
                currentCycle().addPlayerThrow(player, new HashSet<>());
            }
            if (currentCycle().isFinished(maxThrows)) {
                if (maxTradingCyclesReached()) {
                    round.endTradingPhase();
                } else {
                    result = round.getGame().getScoreManager().givePointsForBestHand();
                    tradingCycles.add(new TradingCycle());
                }
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
