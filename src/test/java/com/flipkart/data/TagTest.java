package com.flipkart.data;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Created by anirudh.r on 19/02/16.
 */
public class TagTest {

    @Test
    public void setTag() {
        Tag tag = new Tag("Ads");
        Assert.assertTrue(tag.getId().equals("Ads"));

        Tag tag1 = new Tag("Ads");
        tag1.setId("Buisness");
        Assert.assertTrue(tag1.getId().equals("Buisness"));
    }

    @Test
    public void testEquals() {
        Tag tag = new Tag("Ads");
        Tag tag1 = new Tag("Buisness");

        String string = "123";
        Assert.assertTrue(!tag.equals(tag1));
        Assert.assertTrue(!tag.equals(string));
    }

    @Test
    public void testHashCode() {
        Tag tag = new Tag("Ads");
        Assert.assertTrue(tag.hashCode() == tag.getId().hashCode());
    }
}
