package com.flipkart.batching.core.batch;

import junit.framework.Assert;

import org.junit.Test;

import java.util.Collections;

/**
 * Created by anirudh.r on 11/08/16.
 * Test for {@link SizeTimeBatch}
 */
public class SizeTimeBatchTest {

    /**
     * Test for max size batch
     *
     * @throws Exception
     */
    @Test
    public void testMaxSize() throws Exception {
        SizeTimeBatch sizeTimeBatch = new SizeTimeBatch(Collections.EMPTY_LIST, 10, 1000);
        int maxBatchSize = sizeTimeBatch.getMaxBatchSize();

        //assert that the max batch size is 10
        Assert.assertTrue(maxBatchSize == 10);
    }

    /**
     * Test for timeout
     *
     * @throws Exception
     */
    @Test
    public void testTimeout() throws Exception {
        SizeTimeBatch sizeTimeBatch = new SizeTimeBatch(Collections.EMPTY_LIST, 10, 1000);
        long timeOut = sizeTimeBatch.getTimeOut();

        //assert that the timeout is 1000
        Assert.assertTrue(timeOut == 1000);
    }

    /**
     * Test for equals method
     *
     * @throws Exception
     */
    @Test
    public void testSizeTimeBatchEqualsMethod() throws Exception {
        SizeTimeBatch sizeTimeBatch = new SizeTimeBatch(Collections.EMPTY_LIST, 10, 100);
        SizeTimeBatch sizeTimeBatch1 = new SizeTimeBatch(Collections.EMPTY_LIST, 10, 100);

        //assert that both are equal since the sizes and timeout are same
        Assert.assertTrue(sizeTimeBatch.equals(sizeTimeBatch1));

        sizeTimeBatch = new SizeTimeBatch(Collections.EMPTY_LIST, 10, 100);
        sizeTimeBatch1 = new SizeTimeBatch(Collections.EMPTY_LIST, 10, 200);

        //assert that both are not equal since the time out are not same
        Assert.assertTrue(!sizeTimeBatch.equals(sizeTimeBatch1));
    }
}
