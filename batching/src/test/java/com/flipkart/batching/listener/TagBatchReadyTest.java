package com.flipkart.batching.listener;

import com.flipkart.Utils;
import com.flipkart.batching.BatchingStrategy;
import com.flipkart.batching.BuildConfig;
import com.flipkart.batching.OnBatchReadyListener;
import com.flipkart.batching.data.Tag;
import com.flipkart.batching.data.TagData;
import com.flipkart.batching.strategy.SizeBatchingStrategy;
import com.flipkart.batching.strategy.TagBatchingStrategy;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by anirudh.r on 19/02/16.
 * Test for {@link TagBatchReadyListener}
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class TagBatchReadyTest {


    /**
     * Test to add tag listeners.
     */
    @Test
    public void testAddTagListener() {
        TagBatchReadyListener<TagData> tagBatchReadyListener;
        Tag AD = new Tag("ADS");
        Tag BUSINESS = new Tag("BUSINESS");
        Tag DEBUG = new Tag("DEBUG");

        OnBatchReadyListener<TagData, TagBatchingStrategy.TagBatch<TagData>> onBatchReadyListener = mock(OnBatchReadyListener.class);
        tagBatchReadyListener = new TagBatchReadyListener<>();
        tagBatchReadyListener.addListenerForTag(AD, onBatchReadyListener);
        tagBatchReadyListener.addListenerForTag(DEBUG, onBatchReadyListener);
        tagBatchReadyListener.addListenerForTag(BUSINESS, onBatchReadyListener);

        //assert that the tagMaps are not null
        Assert.assertTrue(!tagBatchReadyListener.getTagOnBatchReadyListenerMap().isEmpty());
    }


    /**
     * Test to verify {@link TagBatchReadyListener#onReady(BatchingStrategy, TagBatchingStrategy.TagBatch)}
     */
    @Test
    public void testOnReady() {
        TagBatchReadyListener<TagData> tagBatchReadyListener;
        Tag AD = new Tag("ADS");
        Tag BUSINESS = new Tag("BUSINESS");
        Tag DEBUG = new Tag("DEBUG");

        OnBatchReadyListener<TagData, TagBatchingStrategy.TagBatch<TagData>> onBatchReadyListener = mock(OnBatchReadyListener.class);
        tagBatchReadyListener = new TagBatchReadyListener<>();
        tagBatchReadyListener.addListenerForTag(AD, onBatchReadyListener);
        tagBatchReadyListener.addListenerForTag(DEBUG, onBatchReadyListener);
        tagBatchReadyListener.addListenerForTag(BUSINESS, onBatchReadyListener);

        tagBatchReadyListener.onReady(new TagBatchingStrategy<>(), new TagBatchingStrategy.TagBatch<>(new Tag("ADS"), new SizeBatchingStrategy.SizeBatch<TagData>(Utils.fakeCollection(2), 4)));
        //verify that it gets called once , when tagBatchReadyListener's onReady gets called
        verify(onBatchReadyListener, times(1)).onReady(any(BatchingStrategy.class), any(TagBatchingStrategy.TagBatch.class));
    }
}
