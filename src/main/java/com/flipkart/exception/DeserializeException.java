package com.flipkart.exception;

/**
 * Created by anirudh.r on 02/02/16.
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
