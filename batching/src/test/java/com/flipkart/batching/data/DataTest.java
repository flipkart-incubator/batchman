package com.flipkart.batching.data;

import com.flipkart.batching.Data;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Created by anirudh.r on 20/02/16.
 * Test to verify {@link Data}
 */
public class DataTest {

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
        Tag AD_TAG = new Tag("ADS");
        TagData tagData = new TagData(AD_TAG, "Event 1");
        Assert.assertTrue(tagData.getTag() == AD_TAG);
    }

    /**
     * Test to verify the equals method in {@link TagData}
     */
    @Test
    public void testTagEqualsData() {
        Tag AD_TAG = new Tag("ADS");
        Tag DEBUG_TAG = new Tag("DEBUG");
        TagData adsTagData = new TagData(AD_TAG, "Event 1");
        TagData debugTagData = new TagData(DEBUG_TAG, "Event 1");
        Assert.assertTrue(!adsTagData.equals(debugTagData));
        Assert.assertTrue(!adsTagData.equals(""));
    }
}
