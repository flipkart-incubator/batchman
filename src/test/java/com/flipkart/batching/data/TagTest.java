package com.flipkart.batching.data;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Created by anirudh.r on 19/02/16.
 */
public class TagTest {

    /**
     * Test to verify {@link Tag#getId()}
     */
    @Test
    public void setTag() {
        Tag tag = new Tag("Ads");
        Assert.assertTrue(tag.getId().equals("Ads"));

        Tag tag1 = new Tag("Ads");
        tag1.setId("Business");
        Assert.assertTrue(tag1.getId().equals("Business"));
    }

    /**
     * Test to verify {@link Tag#equals(Object)}
     */
    @Test
    public void testEquals() {
        Tag tag = new Tag("Ads");
        Tag tag1 = new Tag("Business");

        String string = "123";
        Assert.assertTrue(!tag.equals(tag1));
        Assert.assertTrue(!tag.equals(string));
    }

    /**
     * Test to verify {@link Tag#hashCode()}
     */
    @Test
    public void testHashCode() {
        Tag tag = new Tag("Ads");
        Assert.assertTrue(tag.hashCode() == tag.getId().hashCode());
    }
}
