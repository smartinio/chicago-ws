package io.smartin.id1212.exceptions.game;

import io.smartin.id1212.exceptions.GameException;

public class WaitYourTurnException extends GameException {
    public WaitYourTurnException(String waitYourTurn) {
        super(waitYourTurn);
    }
}
