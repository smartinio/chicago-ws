package io.smartin.id1212.exceptions.game;

import io.smartin.id1212.exceptions.GameException;

public class TooFewPlayersException extends GameException {
    public TooFewPlayersException(String tooFewPlayers) {
        super(tooFewPlayers);
    }
}
