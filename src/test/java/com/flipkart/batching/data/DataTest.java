package com.flipkart.batching.data;

import com.flipkart.batching.Data;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Created by anirudh.r on 20/02/16.
 */
public class DataTest {

    @Test
    public void testData() {
        Data data = new EventData("Event 1");
        Assert.assertTrue(data.getData().toString().equals("Event 1"));
    }

    @Test
    public void testSetData() {
        Data data = new EventData("Event 1");
        data.setData("Event 2");
        Assert.assertTrue(data.getData().toString().equals("Event 2"));
    }

    @Test
    public void testEqualsData() {
        Data data = new EventData("Event 1");
        Assert.assertTrue(!data.getData().toString().equals("hello"));
    }
}
