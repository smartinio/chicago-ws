package io.smartin.id1212.exceptions.game;

import io.smartin.id1212.exceptions.GameException;

public class RoundNotFinishedException extends GameException {
    public RoundNotFinishedException(String message) {
        super(message);
    }
}
