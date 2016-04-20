package com.flipkart.batching.exception;

import java.io.IOException;

/**
 * DeserializationException class that extends {@link IOException}.
 * <p/>
 * To get the real exception use {@link #getRealException()}.
 */
public class DeserializeException extends IOException {
    private final Exception realException;

    public DeserializeException(Exception realException) {
        super(realException.getCause());
        this.realException = realException;
    }

    public Exception getRealException() {
        return realException;
    }
}
