package io.smartin.id1212.exceptions.game;

import io.smartin.id1212.exceptions.GameException;

public class UnauthorizedKickException extends GameException {
    public UnauthorizedKickException(String message) {
        super(message);
    }
}
