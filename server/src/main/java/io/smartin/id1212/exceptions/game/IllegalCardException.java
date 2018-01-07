package io.smartin.id1212.exceptions.game;

import io.smartin.id1212.exceptions.GameException;

public class IllegalCardException extends GameException {
    public IllegalCardException(String illegalCardPlay) {
        super(illegalCardPlay);
    }
}
