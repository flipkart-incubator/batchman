package com.flipkart.batching.exception;

import junit.framework.Assert;

import org.junit.Test;

import java.io.IOException;

/**
 * Created by anirudh.r on 20/02/16.
 * Test for {@link DeserializeException}
 */
public class DeserializeExceptionTest {

    /**
     * Test to verify {@link DeserializeException}
     */
    @Test
    public void testDeserializeException() {
        DeserializeException deserializeException = new DeserializeException(new IOException());
        Assert.assertNotNull(deserializeException.getRealException());
    }
}
