package com.flipkart.batching;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.flipkart.data.Data;
import com.flipkart.data.EventData;
import com.flipkart.data.Tag;
import com.flipkart.persistence.PersistenceStrategy;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by anirudh.r on 13/02/16.
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class TagBatchingTest {

    Tag AD_TAG = new Tag("AD");
    Tag DEBUG_TAG = new Tag("DEBUG");
    Tag BUISNESS_TAG = new Tag("BUISNESS");
    private TagBatchingStrategy tagBatchingStrategy;
    private SizeBatchingStrategy sizeBatchingStrategy;
    @Mock
    private PersistenceStrategy persistenceStrategy;
    @Mock
    private BatchController batchController;
    @Mock
    private Context context;
    @Mock
    private OnBatchReadyListener onBatchReadyListener;
    private ShadowLooper shadowLooper;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test to verify that {@link TagBatchingStrategy#onDataPushed(Collection)} is working
     */
    @Test
    public void testOnDataPushed() {
        initializeTagBatching();
        Data eventData = new EventData(AD_TAG, "");
        Set<Data> singleton = Collections.singleton(eventData);
        tagBatchingStrategy.onDataPushed(singleton);
        //verify that add method is called once.
        verify(persistenceStrategy, times(1)).add(eq(singleton));
    }

    /**
     * Initialize the TagBatchingStrategy
     */
    private void initializeTagBatching() {
        tagBatchingStrategy = new TagBatchingStrategy();
        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        shadowLooper = Shadows.shadowOf(looper);
        Handler handler = new Handler(looper);
        sizeBatchingStrategy = new SizeBatchingStrategy(5, persistenceStrategy);
        tagBatchingStrategy.addTagStrategy(AD_TAG, sizeBatchingStrategy);
        tagBatchingStrategy.onInitialized(batchController, context, onBatchReadyListener, handler);
    }
}
