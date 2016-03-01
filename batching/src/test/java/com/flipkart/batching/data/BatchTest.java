package com.flipkart.batching.data;

import com.flipkart.Utils;
import com.flipkart.batching.Batch;
import com.flipkart.batching.Data;

import junit.framework.Assert;

import org.junit.Test;

import java.util.ArrayList;

/**
 * Created by anirudh.r on 20/02/16.
 */
public class BatchTest {

    /**
     * Test to verify the {@link Batch#getDataCollection()} method.
     */
    @Test
    public void testBatch() {
        ArrayList<Data> dataArrayList = Utils.fakeCollection(4);
        Batch<Data> batch = new Batch<>(dataArrayList);
        Assert.assertEquals(batch.getDataCollection(), dataArrayList);
    }

    /**
     * Test to verify that {@link Batch#equals(Object)} method.
     */
    @Test
    public void testBatchEquals() {
        ArrayList<Data> arrayList = Utils.fakeCollection(4);
        ArrayList<Data> arrayList1 = Utils.fakeCollection(5);
        Batch<Data> batch = new Batch<>(arrayList);
        Batch<Data> batch1 = new Batch<>(arrayList1);
        Assert.assertTrue(!batch.equals(batch1));
    }
}
