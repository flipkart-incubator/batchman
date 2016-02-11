package com.flipkart.exception;

/**
 * Created by anirudh.r on 02/02/16.
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

