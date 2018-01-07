package io.smartin.id1212.exceptions.game;

import io.smartin.id1212.exceptions.GameException;

public class ChicagoAlreadyCalledException extends GameException {
    public ChicagoAlreadyCalledException(String chicagoAlreadyCalled) {
        super(chicagoAlreadyCalled);
    }
}
