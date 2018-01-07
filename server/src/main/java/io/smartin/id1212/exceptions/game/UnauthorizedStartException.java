package io.smartin.id1212.exceptions.game;

import io.smartin.id1212.exceptions.GameException;

public class UnauthorizedStartException extends GameException {
    public UnauthorizedStartException(String unauthorizedStart) {
        super(unauthorizedStart);
    }
}
