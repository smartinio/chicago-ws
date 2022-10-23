package io.smartin.id1212.exceptions.game;

import io.smartin.id1212.exceptions.GameException;

public class TooManyCardsException extends GameException {
    public TooManyCardsException(String s) {
        super(s);
    }
}
