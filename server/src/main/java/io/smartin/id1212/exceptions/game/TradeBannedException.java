package io.smartin.id1212.exceptions.game;

import io.smartin.id1212.exceptions.GameException;

public class TradeBannedException extends GameException {
    public TradeBannedException(String tradeBanned) {
        super(tradeBanned);
    }
}
