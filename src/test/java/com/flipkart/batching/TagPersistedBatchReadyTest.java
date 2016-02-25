package com.flipkart.batching;

import com.flipkart.Utils;
import com.flipkart.batching.data.Tag;
import com.flipkart.batching.listener.TagBatchReadyListener;
import com.flipkart.batching.strategy.SizeBatchingStrategy;
import com.flipkart.batching.strategy.TagBatchingStrategy;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by anirudh.r on 19/02/16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class TagPersistedBatchReadyTest {

    private TagBatchReadyListener tagBatchReadyListener;
    private Tag AD = new Tag("ADS");
    private Tag BUSINESS = new Tag("BUSINESS");
    private Tag DEBUG = new Tag("DEBUG");
    @Mock
    private OnBatchReadyListener onBatchReadyListener;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test to add tag listeners.
     */
    @Test
    public void testAddTagListener() {

        tagBatchReadyListener = new TagBatchReadyListener();
        tagBatchReadyListener.addListenerForTag(AD, onBatchReadyListener);
        tagBatchReadyListener.addListenerForTag(DEBUG, onBatchReadyListener);
        tagBatchReadyListener.addListenerForTag(BUSINESS, onBatchReadyListener);

        Assert.assertTrue(!tagBatchReadyListener.getTagOnBatchReadyListenerMap().isEmpty());
    }

    @Test
    public void testOnReady() {
        tagBatchReadyListener = new TagBatchReadyListener();
        tagBatchReadyListener.addListenerForTag(AD, onBatchReadyListener);
        tagBatchReadyListener.addListenerForTag(DEBUG, onBatchReadyListener);
        tagBatchReadyListener.addListenerForTag(BUSINESS, onBatchReadyListener);

        tagBatchReadyListener.onReady(new TagBatchingStrategy(), new TagBatchingStrategy.TagBatch(new Tag("ADS"), new SizeBatchingStrategy.SizeBatch(Utils.fakeCollection(2), 4)));

        verify(onBatchReadyListener, times(1)).onReady(any(BatchingStrategy.class), any(Batch.class));
    }

//    @Test(expected = IllegalStateException.class)
//    public void testExceptionThrown() {
//        tagBatchReadyListener = new TagBatchReadyListener();
//        tagBatchReadyListener.addListenerForTag(AD, onBatchReadyListener);
//        tagBatchReadyListener.addListenerForTag(DEBUG, onBatchReadyListener);
//        tagBatchReadyListener.addListenerForTag(BUSINESS, onBatchReadyListener);
//
//        ArrayList<Data> arrayList = Utils.fakeCollection(4);
//        tagBatchReadyListener.onReady(new SizeBatchingStrategy(4, new InMemoryPersistenceStrategy()),
//                new SizeBatchingStrategy.SizeBatch<>(Utils.fakeCollection(5), 5));
//
//    }
}
