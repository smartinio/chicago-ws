package io.smartin.id1212.exceptions.game;

import io.smartin.id1212.exceptions.GameException;

public class InappropriateActionException extends GameException {
    public InappropriateActionException(String inappropriateAction) {
        super(inappropriateAction);
    }
}
