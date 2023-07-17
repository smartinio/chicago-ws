package io.smartin.id1212.exceptions.game;

import io.smartin.id1212.exceptions.GameException;

public class UnknownActionException extends GameException {
    public UnknownActionException(String unknownAction) {
        super(unknownAction);
    }
}
