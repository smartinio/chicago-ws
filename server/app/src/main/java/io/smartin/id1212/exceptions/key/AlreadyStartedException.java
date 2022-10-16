package io.smartin.id1212.exceptions.key;

import io.smartin.id1212.exceptions.KeyException;

public class AlreadyStartedException extends KeyException {
    public AlreadyStartedException(String msg) {
        super(msg);
    }
}
