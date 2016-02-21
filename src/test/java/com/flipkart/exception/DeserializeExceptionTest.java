package com.flipkart.exception;

import junit.framework.Assert;

import org.junit.Test;

import java.io.IOException;

/**
 * Created by anirudh.r on 20/02/16.
 */
public class DeserializeExceptionTest {


    @Test
    public void testSerializeException() {
        DeserializeException deserializeException = new DeserializeException(new IOException());
        Assert.assertNotNull(deserializeException.getRealException());
    }
}
