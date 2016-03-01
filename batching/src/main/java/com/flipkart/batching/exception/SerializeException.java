package com.flipkart.batching.exception;

/**
 * SerializationException class that extends {@link Exception}.
 * <p>
 * To get the real exception use {@link #getRealException()}.
 */

public class SerializeException extends Exception {

    private final Exception realException;

    public SerializeException(Exception realException) {
        super(realException.getCause());
        this.realException = realException;
    }

    public Exception getRealException() {
        return realException;
    }
}

