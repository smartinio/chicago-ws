package io.smartin.id1212.exceptions;

public class FatalException extends Throwable {
    protected FatalException() {
    }

    public FatalException(String message) {
        super(message);
    }
}
