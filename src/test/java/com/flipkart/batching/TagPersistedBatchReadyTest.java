package com.flipkart.batching;

import com.flipkart.Utils;
import com.flipkart.data.Data;
import com.flipkart.data.Tag;
import com.flipkart.persistence.InMemoryPersistenceStrategy;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by anirudh.r on 19/02/16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class TagPersistedBatchReadyTest {

    private TagPersistedBatchReadyListener tagPersistedBatchReadyListener;
    private Tag AD = new Tag("ADS");
    private Tag BUISNESS = new Tag("BUISNESS");
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

        tagPersistedBatchReadyListener = new TagPersistedBatchReadyListener();
        tagPersistedBatchReadyListener.addListenerForTag(AD, onBatchReadyListener);
        tagPersistedBatchReadyListener.addListenerForTag(DEBUG, onBatchReadyListener);
        tagPersistedBatchReadyListener.addListenerForTag(BUISNESS, onBatchReadyListener);

        Assert.assertTrue(!tagPersistedBatchReadyListener.getTagOnBatchReadyListenerMap().isEmpty());
    }

    @Test
    public void testOnReady() {
        tagPersistedBatchReadyListener = new TagPersistedBatchReadyListener();
        tagPersistedBatchReadyListener.addListenerForTag(AD, onBatchReadyListener);
        tagPersistedBatchReadyListener.addListenerForTag(DEBUG, onBatchReadyListener);
        tagPersistedBatchReadyListener.addListenerForTag(BUISNESS, onBatchReadyListener);

        ArrayList<Data> arrayList = Utils.fakeCollection(4);
        tagPersistedBatchReadyListener.onReady(new TagBatchingStrategy(), new TagBatchingStrategy.TagBatchInfo
                (AD, new SizeBatchingStrategy.SizeBatchInfo(5)), arrayList);

        verify(onBatchReadyListener, times(1)).onReady(any(BatchingStrategy.class), any(BatchInfo.class), eq(arrayList));
    }

    @Test(expected = IllegalStateException.class)
    public void testExceptionThrown() {
        tagPersistedBatchReadyListener = new TagPersistedBatchReadyListener();
        tagPersistedBatchReadyListener.addListenerForTag(AD, onBatchReadyListener);
        tagPersistedBatchReadyListener.addListenerForTag(DEBUG, onBatchReadyListener);
        tagPersistedBatchReadyListener.addListenerForTag(BUISNESS, onBatchReadyListener);

        ArrayList<Data> arrayList = Utils.fakeCollection(4);
        tagPersistedBatchReadyListener.onReady(new SizeBatchingStrategy(4, new InMemoryPersistenceStrategy()),
                new SizeBatchingStrategy.SizeBatchInfo(5), arrayList);

    }
}
