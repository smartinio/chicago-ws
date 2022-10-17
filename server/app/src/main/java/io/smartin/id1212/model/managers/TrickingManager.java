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
    public class MoveResult {
        public final Move winningMove;
        public final boolean hasMoreTricks;
        public final boolean isTrickDone;

        public MoveResult(Move winningMove, boolean hasMoreTricks, boolean isTrickDone) {
            this.winningMove = winningMove;
            this.hasMoreTricks = hasMoreTricks;
            this.isTrickDone = isTrickDone;
        }
    }

    private Round round;
    @Expose
    private List<Trick> tricks = new ArrayList<>();

    public TrickingManager(Round round) {
        this.round = round;
        this.tricks.add(new Trick(round));
    }

    public MoveResult handle(Move move) throws IllegalMoveException {
        currentTrick().addMove(move);
        move.getPlayer().getHand().moveToPlayed(move.getCard());
        Move winningMove = currentTrick().getWinningMove();
        boolean hasMoreTricks = !thisWasLastTrick();
        boolean isTrickDone = currentTrick().isDone();
        MoveResult result = new MoveResult(winningMove, hasMoreTricks, isTrickDone);

        if (isTrickDone) {
            this.tricks.add(new Trick(round));
        }

        return result;
    }

    private boolean thisWasLastTrick() {
        return tricks.size() == MAX_CARDS_PER_PLAYER && currentTrick().isDone();
    }

    public Trick currentTrick() {
        return tricks.get(tricks.size() - 1);
    }

    public List<Trick> getTricks() {
        return tricks;
    }
}
