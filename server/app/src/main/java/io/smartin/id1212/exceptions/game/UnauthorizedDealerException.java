package io.smartin.id1212.exceptions.game;

import io.smartin.id1212.exceptions.GameException;

public class UnauthorizedDealerException extends GameException {
    public UnauthorizedDealerException(String message) {
        super(message);
    }
}
