package io.smartin.id1212.model.managers;

import io.smartin.id1212.exceptions.game.OutOfCardsException;
import io.smartin.id1212.model.components.*;
import io.smartin.id1212.model.managers.ScoreManager.BestHandResult;

import java.util.ArrayList;
import java.util.List;

import static io.smartin.id1212.config.Rules.MAX_TRADES_PER_ROUND;

public class TradingManager {
    private Round round;
    private List<TradingCycle> tradingCycles = new ArrayList<>();

    public TradingManager(Round round) {
        this.round = round;
        this.tradingCycles.add(new TradingCycle());
    }

    public List<BestHandResult> handle(Player player, List<PlayingCard> cards) throws OutOfCardsException {
        int maxThrows = round.getGame().getPlayers().size();
        List<BestHandResult> result = new ArrayList<>();

        if (cards.size() > 0) {
            player.removeCards(cards);
            currentCycle().addPlayerThrow(player, cards);
            try {
                player.giveCards(round.getDeck().draw(cards.size()));
            } catch (OutOfCardsException e) {
                round.getDeck().addCards(thrownCards().draw(cards.size() - round.getDeck().size()));
            }
        } else {
            currentCycle().addPlayerThrow(player, new ArrayList<>());
        }
        if (currentCycle().isFinished(maxThrows)) {
            if (maxTradingCyclesReached()) {
                round.endTradingPhase();
            } else {
                result = round.getGame().getScoreManager().givePointsForBestHand();
                tradingCycles.add(new TradingCycle());
            }
        }

        return result;
    }

    private boolean maxTradingCyclesReached() {
        return tradingCycles.size() == MAX_TRADES_PER_ROUND;
    }

    private CardDeck thrownCards() {
        CardDeck cardDeck = new CardDeck(false);
        for (TradingCycle tradingCycle : tradingCycles) {
            cardDeck.addCards(tradingCycle.getCards());
        }
        return cardDeck;
    }

    private TradingCycle currentCycle() {
        return tradingCycles.get(tradingCycles.size() - 1);
    }
}
