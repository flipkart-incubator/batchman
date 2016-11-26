package com.flipkart.batching_core.batch;

import com.flipkart.batching_core.data.Tag;

import junit.framework.Assert;

import org.junit.Test;

import java.util.Collections;

/**
 * Created by anirudh.r on 11/08/16.
 * Test for {@link TagBatch}
 */
public class TagBatchTest {

    /**
     * Test for get tag
     *
     * @throws Exception
     */
    @Test
    public void testGetTag() throws Exception {
        TagBatch tagBatch = new TagBatch(new Tag("test"), new SizeBatch(Collections.EMPTY_LIST, 10));
        Tag tag = tagBatch.getTag();
        String id = tag.getId();

        //assert that the id are same
        Assert.assertTrue(id.equals("test"));
    }

    /**
     * Test for equals method
     *
     * @throws Exception
     */
    @Test
    public void testEqualsMethod() throws Exception {
        TagBatch tagBatch = new TagBatch(new Tag("test"), new SizeBatch(Collections.EMPTY_LIST, 10));
        TagBatch tagBatch1 = new TagBatch(new Tag("test"), new SizeBatch(Collections.EMPTY_LIST, 10));

        //assert that both tag batch are same
        Assert.assertTrue(tagBatch.equals(tagBatch1));

        tagBatch = new TagBatch(new Tag("test"), new SizeBatch(Collections.EMPTY_LIST, 10));
        tagBatch1 = new TagBatch(new Tag("test1"), new SizeBatch(Collections.EMPTY_LIST, 10));

        //assert that both tag batch are not same since they have diff tag
        Assert.assertTrue(!tagBatch.equals(tagBatch1));
    }
}
