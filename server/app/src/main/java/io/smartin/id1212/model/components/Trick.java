package io.smartin.id1212.model.components;

import com.google.gson.annotations.Expose;
import io.smartin.id1212.exceptions.game.IllegalMoveException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.smartin.id1212.config.Strings.MUST_FOLLOW_SUIT;

public class Trick {
    @Expose
    private List<Move> moves = new ArrayList<>();
    private transient Round round;

    public Trick(Round round) {
        this.round = round;
    }

    public void addMove(Move move) throws IllegalMoveException {
        validate(move);
        moves.add(move);
    }

    private void validate(Move move) throws IllegalMoveException {
        if (moves.size() == 0) {
            return;
        }
        if (followsSuit(move)) {
            return;
        }
        if (!playerHasSuit(move)) {
            return;
        }
        throw new IllegalMoveException(MUST_FOLLOW_SUIT);
    }

    private boolean playerHasSuit(Move move) {
        return move.getPlayer().hasCardOfSuit(getStartingMove().getCard().getSuit());
    }

    private boolean followsSuit(Move move) {
        return move.getCard().getSuit().equals(getStartingMove().getCard().getSuit());
    }

    public boolean isDone() {
        return moves.size() == round.getGame().getPlayers().size();
    }

    public Move getWinningMove() {
        Move bestMove = getStartingMove();

        for (Move move : moves) {
            if (move.beats(bestMove)) {
                bestMove = move;
            }
        }

        return bestMove;
    }

    public Move getStartingMove() {
        return moves.get(0);
    }

    public Set<Player> getRemainingPlayers() {
        List<Player> allPlayers = round.getGame().getPlayers();
        Set<Player> remainingPlayers = new HashSet<>(allPlayers);

        for (Move move : moves) {
            remainingPlayers.remove(move.getPlayer());
        }

        return remainingPlayers;
    }

    public Set<Player> getAlreadyPlayedPlayers() {
        Set<Player> alreadyPlayed = new HashSet<>();

        for (Move move : moves) {
            alreadyPlayed.add(move.getPlayer());
        }

        return alreadyPlayed;
    }
}
