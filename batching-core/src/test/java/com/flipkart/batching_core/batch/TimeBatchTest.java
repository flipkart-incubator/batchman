package com.flipkart.batching_core.batch;

import junit.framework.Assert;

import org.junit.Test;

import java.util.Collections;

/**
 * Created by anirudh.r on 11/08/16.
 * Test for {@link TimeBatch}
 */
public class TimeBatchTest {

    /**
     * Test for time out getters
     *
     * @throws Exception
     */
    @Test
    public void testTimeOut() throws Exception {
        TimeBatch timeBatch = new TimeBatch(Collections.EMPTY_LIST, 1000);
        long timeOut = timeBatch.getTimeOut();

        //assert that time out is equal to 1000
        Assert.assertTrue(timeOut == 1000);
    }

    /**
     * Test for equals method in TimeBatch
     *
     * @throws Exception
     */
    @Test
    public void testEqualsMethod() throws Exception {
        TimeBatch timeBatch = new TimeBatch(Collections.EMPTY_LIST, 1000);
        TimeBatch timeBatch1 = new TimeBatch(Collections.EMPTY_LIST, 1000);

        //assert that both the batches are same
        Assert.assertTrue(timeBatch.equals(timeBatch1));

        timeBatch = new TimeBatch(Collections.EMPTY_LIST, 2000);
        timeBatch1 = new TimeBatch(Collections.EMPTY_LIST, 1000);

        //assert that both the batches are not same
        Assert.assertTrue(!timeBatch.equals(timeBatch1));
    }
}
