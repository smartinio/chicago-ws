package io.smartin.id1212.exceptions.game;

import io.smartin.id1212.exceptions.GameException;

public class IllegalMoveException extends GameException {
    public IllegalMoveException(String mustFollowSuit) {
        super(mustFollowSuit);
    }
}
