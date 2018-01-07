package io.smartin.id1212.model.managers;

import com.google.gson.annotations.Expose;
import io.smartin.id1212.exceptions.game.IllegalMoveException;
import io.smartin.id1212.exceptions.game.TrickNotDoneException;
import io.smartin.id1212.model.components.Move;
import io.smartin.id1212.model.components.Player;
import io.smartin.id1212.model.components.Round;
import io.smartin.id1212.model.components.Trick;

import java.util.ArrayList;
import java.util.List;

import static io.smartin.id1212.config.Rules.MAX_CARDS_PER_PLAYER;

public class TrickingManager {
    private Round round;
    @Expose
    private List<Trick> tricks = new ArrayList<>();

    public TrickingManager(Round round) {
        this.round = round;
        this.tricks.add(new Trick(round));
    }

    public Player handle(Move move) throws IllegalMoveException, TrickNotDoneException {
        currentTrick().addMove(move);
        move.getPlayer().getHand().moveToPlayed(move.getCard());
        if (round.hasChicagoCalled()) {
            if (!chicagoCallerIsWinningTrick()) {
                round.setPhase(Round.GamePhase.AFTER);
                round.getGame().finishChicagoCalledRound(false, round.getChicagoTaker());
                return currentTrick().getWinningMove().getPlayer();
            }
        }
        if (thisWasLastTrick()) {
            round.setPhase(Round.GamePhase.AFTER);
            if (round.hasChicagoCalled()) {
                round.getGame().finishChicagoCalledRound(true, round.getChicagoTaker());
                return round.getChicagoTaker();
            }
            round.getGame().finishNormalRound();
            return currentTrick().getWinningMove().getPlayer();
        }
        if (currentTrick().isDone()) {
            Trick lastTrick = currentTrick();
            this.tricks.add(new Trick(round));
            return lastTrick.getWinningMove().getPlayer();
        }
        throw new TrickNotDoneException();
    }

    private boolean thisWasLastTrick() {
        return tricks.size() == MAX_CARDS_PER_PLAYER && currentTrick().isDone();
    }

    private boolean chicagoCallerIsWinningTrick() {
        return currentTrick().getWinningMove().getPlayer().equals(round.getChicagoTaker());
    }

    public Trick currentTrick() {
        return tricks.get(tricks.size() - 1);
    }

    public List<Trick> getTricks() {
        return tricks;
    }
}
