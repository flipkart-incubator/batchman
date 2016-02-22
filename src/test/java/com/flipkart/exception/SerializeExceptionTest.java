package com.flipkart.exception;

import junit.framework.Assert;

import org.junit.Test;

import java.io.IOException;

/**
 * Created by anirudh.r on 20/02/16.
 */
public class SerializeExceptionTest {

    @Test
    public void testSerializeException() {
        SerializeException serializeException = new SerializeException(new IOException());
        Assert.assertNotNull(serializeException.getRealException());
    }
}
