package com.flipkart.batching.exception;

import java.io.IOException;

/**
 * SerializationException class that extends {@link IOException}.
 * <p/>
 * To get the real exception use {@link #getRealException()}.
 */
public class SerializeException extends IOException {
    private final Exception realException;

    public SerializeException(Exception realException) {
        super(realException.getCause());
        this.realException = realException;
    }

    public Exception getRealException() {
        return realException;
    }
}

