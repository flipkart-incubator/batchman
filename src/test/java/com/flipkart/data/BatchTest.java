package com.flipkart.data;

import com.flipkart.Utils;
import com.flipkart.batching.BatchInfo;
import com.flipkart.batching.SizeBatchingStrategy;
import com.flipkart.batching.TimeBatchingStrategy;

import junit.framework.Assert;

import org.junit.Test;

import java.util.ArrayList;

/**
 * Created by anirudh.r on 20/02/16.
 */
public class BatchTest {

    @Test
    public void testBatchInfo() {
        BatchInfo batchInfo = new SizeBatchingStrategy.SizeBatchInfo(5);
        Batch batch = new Batch(batchInfo, Utils.fakeCollection(4));
        Assert.assertEquals(batchInfo, batch.getBatchInfo());
    }

    @Test
    public void testBatchInfoCollection() {
        ArrayList<Data> arrayList = Utils.fakeCollection(4);
        BatchInfo batchInfo = new SizeBatchingStrategy.SizeBatchInfo(5);
        Batch batch = new Batch(batchInfo, arrayList);
        Assert.assertEquals(arrayList, batch.getDataCollection());
    }

    @Test
    public void testBatchEquals() {
        ArrayList<Data> arrayList = Utils.fakeCollection(4);
        BatchInfo batchInfo = new SizeBatchingStrategy.SizeBatchInfo(5);
        BatchInfo timeBatchInfo = new TimeBatchingStrategy.TimeBatchInfo(5000);
        Batch batch = new Batch(batchInfo, arrayList);
        Batch batch1 = new Batch(timeBatchInfo, arrayList);

        Assert.assertTrue(!batch.equals(batch1));
        Assert.assertTrue(!batch.equals("Batching"));
        Assert.assertTrue(!batch1.equals("Batching"));
    }
}
