package com.flipkart.batching_core.batch;

import junit.framework.Assert;

import org.junit.Test;

import java.util.Collections;

/**
 * Created by anirudh.r on 11/08/16.
 * Test for {@link SizeBatch}
 */
public class SizeBatchTest {

    /**
     * Test to verify the max batch size getters
     *
     * @throws Exception
     */
    @Test
    public void testMaxSize() throws Exception {
        SizeBatch sizeBatch = new SizeBatch(Collections.EMPTY_LIST, 10);
        int maxBatchSize = sizeBatch.getMaxBatchSize();
        Assert.assertTrue(maxBatchSize == 10);
    }

    @Test
    public void testSizeBatchEqualsMethod() throws Exception {
        SizeBatch sizeBatch = new SizeBatch(Collections.EMPTY_LIST, 10);
        SizeBatch sizeBatch1 = new SizeBatch(Collections.EMPTY_LIST, 10);

        //assert that both are equal since the sizes are same
        Assert.assertTrue(sizeBatch.equals(sizeBatch1));

        sizeBatch = new SizeBatch(Collections.EMPTY_LIST, 10);
        sizeBatch1 = new SizeBatch(Collections.EMPTY_LIST, 11);

        //assert that both are not equal since the sizes are not same
        Assert.assertTrue(!sizeBatch.equals(sizeBatch1));
    }
}
