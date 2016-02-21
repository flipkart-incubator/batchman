package com.flipkart.data;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Created by anirudh.r on 20/02/16.
 */
public class DataTest {

    Tag AD = new Tag("AD");
    Tag DEBUG = new Tag("DEBUG");

    @Test
    public void testData() {
        Data data = new EventData(AD, "Event 1");
        Assert.assertTrue(data.getData().toString().equals("Event 1"));
    }

    @Test
    public void testSetData() {
        Data data = new EventData(AD, "Event 1");
        data.setData("Event 2");
        Assert.assertTrue(data.getData().toString().equals("Event 2"));
    }

    @Test
    public void testTag() {
        Data data = new EventData(AD, "Event 1");
        data.setTag(DEBUG);
        Assert.assertTrue(data.getTag().equals(DEBUG));
    }

    @Test
    public void testEqualsData() {
        Data data = new EventData(AD, "Event 1");
        Data data1 = new EventData(AD, "Event 1");
        Assert.assertTrue(!data.equals(data1));
        Assert.assertTrue(!data.equals("hello"));
    }
}
