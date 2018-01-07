package io.smartin.id1212.exceptions.game;

import io.smartin.id1212.exceptions.GameException;

public class NotInGameException extends GameException {
    public NotInGameException(String invalidPlayer) {
        super(invalidPlayer);
    }
}
