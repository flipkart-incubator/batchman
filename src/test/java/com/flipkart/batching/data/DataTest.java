package com.flipkart.batching.data;

import com.flipkart.batching.Data;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Created by anirudh.r on 20/02/16.
 */
public class DataTest {

    Tag AD_TAG = new Tag("ADS");

    /**
     * Test to verify {@link Data#equals(Object)}
     */
    @Test
    public void testData() {
        Data data = new EventData("Event 1");
        Assert.assertTrue(data.getData().toString().equals("Event 1"));
    }

    /**
     * Test o verify {@link Data#getData()} method
     */
    @Test
    public void testSetData() {
        Data data = new EventData("Event 1");
        data.setData("Event 2");
        Assert.assertTrue(data.getData().toString().equals("Event 2"));
    }

    /**
     * Test to verify {@link Data#equals(Object)}
     */
    @Test
    public void testEqualsData() {
        Data data = new EventData("Event 1");
        Assert.assertTrue(!data.getData().toString().equals("hello"));
        Assert.assertTrue(!data.equals("e"));
    }

    /**
     * Test to verify {@link TagData}
     */
    @Test
    public void testTagData() {
        TagData tagData = new TagData(AD_TAG, "Event 1");
        Assert.assertTrue(tagData.getTag() == AD_TAG);
    }
}
