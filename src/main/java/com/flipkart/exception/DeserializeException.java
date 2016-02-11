package com.flipkart.exception;

/**
 * DeserializationException class that extends {@link Exception}.
 * <p/>
 * To get the real exception use {@link #getRealException()}.
 */

public class DeserializeException extends Exception {

    private final Exception realException;

    public DeserializeException(Exception realException) {
        super(realException.getCause());
        this.realException = realException;
    }

    public Exception getRealException() {
        return realException;
    }
}
