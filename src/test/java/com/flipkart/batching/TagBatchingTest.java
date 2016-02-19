package com.flipkart.batching;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.flipkart.Utils;
import com.flipkart.data.Data;
import com.flipkart.data.Tag;
import com.flipkart.persistence.InMemoryPersistenceStrategy;
import com.flipkart.persistence.SQLPersistenceStrategy;
import com.flipkart.persistence.TagBasedPersistenceStrategy;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by anirudh.r on 13/02/16.
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class TagBatchingTest {

    private static final int BATCH_SIZE = 5;
    private static final long TIME_OUT = 5000;
    private Tag AD_TAG = new Tag("AD");
    private Tag DEBUG_TAG = new Tag("DEBUG");
    private Tag BUSINESS_TAG = new Tag("BUISNESS");
    private SizeBatchingStrategy sizeBatchingStrategy;
    private TimeBatchingStrategy timeBatchingStrategy;
    private ComboBatchingStrategy comboBatchingStrategy;
    private TagBatchingStrategy tagBatchingStrategy;
    private ShadowLooper shadowLooper;
    @Mock
    private InMemoryPersistenceStrategy inMemoryPersistenceStrategy;
    @Mock
    private SQLPersistenceStrategy sqlPersistenceStrategy;
    @Mock
    private BatchController batchController;
    @Mock
    private Context context;
    @Mock
    private PersistedBatchReadyListener onBatchReadyListener;

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
        ArrayList<Data> adsDataArrayList = Utils.fakeAdsCollection(2);
        ArrayList<Data> debugDataArrayList = Utils.fakeDebugCollection(2);
        ArrayList<Data> arrayList = new ArrayList<>();
        arrayList.addAll(adsDataArrayList);
        arrayList.addAll(debugDataArrayList);

        tagBatchingStrategy.onDataPushed(arrayList);
        //verify that add method is called once for every data.
        verify(inMemoryPersistenceStrategy, times(1)).add(Collections.singleton(adsDataArrayList.get(0)));
        verify(inMemoryPersistenceStrategy, times(1)).add(Collections.singleton(adsDataArrayList.get(1)));
        verify(sqlPersistenceStrategy, times(1)).add(Collections.singleton(debugDataArrayList.get(0)));
        verify(sqlPersistenceStrategy, times(1)).add(Collections.singleton(debugDataArrayList.get(1)));
    }

    /**
     * Test to verify that {@link TagBatchingStrategy#flush(boolean)} is calling the respective
     * {@link com.flipkart.persistence.PersistenceStrategy#removeData(Collection)} method
     */
    @Test
    public void testFlush() {
        initializeTagBatching();
        ArrayList<Data> adsDataArrayList = Utils.fakeAdsCollection(2);
        ArrayList<Data> debugDataArrayList = Utils.fakeDebugCollection(2);
        ArrayList<Data> arrayList = new ArrayList<>();
        arrayList.addAll(adsDataArrayList);
        arrayList.addAll(debugDataArrayList);

        tagBatchingStrategy.onDataPushed(arrayList);
        when(inMemoryPersistenceStrategy.getData()).thenReturn(adsDataArrayList);
        when(sqlPersistenceStrategy.getData()).thenReturn(debugDataArrayList);
        tagBatchingStrategy.flush(true);

        verify(inMemoryPersistenceStrategy, times(1)).removeData(eq(adsDataArrayList));
    }

    /**
     * This test is to check the {@link OnBatchReadyListener#onReady(BatchingStrategy, BatchInfo, Collection)}  callback for various uses cases.
     * Flush is TRUE for this test. {@link OnBatchReadyListener#onReady(BatchingStrategy, BatchInfo, Collection)}  should be called every time.
     */
    @Test
    public void testOnReadyCallbackFlushTrue() {
        initializeTagBatching();
        //verify that onReady is called, as flush force is true
        ArrayList<Data> adsDataArrayList = Utils.fakeAdsCollection(2);
        ArrayList<Data> debugDataArrayList = Utils.fakeDebugCollection(2);
        ArrayList<Data> arrayList = new ArrayList<>();
        arrayList.addAll(adsDataArrayList);
        arrayList.addAll(debugDataArrayList);
        tagBatchingStrategy.onDataPushed(arrayList);
        when(inMemoryPersistenceStrategy.getData()).thenReturn(adsDataArrayList);
        when(sqlPersistenceStrategy.getData()).thenReturn(debugDataArrayList);
        tagBatchingStrategy.flush(true);
        verify(onBatchReadyListener, times(1)).onReady(eq(tagBatchingStrategy), any(TagBatchingStrategy.TagBatchInfo.class), eq(adsDataArrayList));
        verify(onBatchReadyListener, times(1)).onReady(eq(tagBatchingStrategy), any(TagBatchingStrategy.TagBatchInfo.class), eq(debugDataArrayList));
    }

    /**
     * This test is to check the {@link OnBatchReadyListener#onReady(BatchingStrategy, BatchInfo, Collection)} callback.
     * This test ensures the integrity of the data.
     */
    @Test
    public void testOnReadyCallbackData() {
        initializeTagBatching();
        ArrayList<Data> adsDataList = Utils.fakeAdsCollection(5);
        tagBatchingStrategy.onDataPushed(adsDataList);
        when(inMemoryPersistenceStrategy.getData()).thenReturn(adsDataList);
        tagBatchingStrategy.flush(false);
        verify(onBatchReadyListener, times(1)).onReady(eq(tagBatchingStrategy), any(TagBatchingStrategy.TagBatchInfo.class), eq(adsDataList));

        initializeTagBatching();
        reset(onBatchReadyListener);
        ArrayList<Data> debugDataList = Utils.fakeDebugCollection(5);
        tagBatchingStrategy.onDataPushed(debugDataList);
        when(sqlPersistenceStrategy.getData()).thenReturn(debugDataList);
        tagBatchingStrategy.flush(false);
        shadowLooper.idle(5000);
        verify(onBatchReadyListener, times(1)).onReady(eq(tagBatchingStrategy), any(TagBatchingStrategy.TagBatchInfo.class), eq(debugDataList));
    }

    /**
     * Test to verify that {@link TagBatchingStrategy#addTagStrategy(Tag, BatchingStrategy)}
     */
    @Test
    public void testAddStrategy() {
        Map<Tag, BatchingStrategy> batchingStrategyMap = new HashMap<>();
        batchingStrategyMap.put(AD_TAG, new SizeBatchingStrategy(5, inMemoryPersistenceStrategy));
        batchingStrategyMap.put(DEBUG_TAG, new SizeBatchingStrategy(5, sqlPersistenceStrategy));
        batchingStrategyMap.put(BUSINESS_TAG, new SizeBatchingStrategy(5, inMemoryPersistenceStrategy));
        Assert.assertTrue(batchingStrategyMap.size() == 3);
    }

    @Test
    public void testOnInitialized() {
        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper);
        TagBatchingStrategy tagBatchingStrategy = new TagBatchingStrategy();
        tagBatchingStrategy.onInitialized(batchController, context, onBatchReadyListener, handler);

        Assert.assertTrue(tagBatchingStrategy.isInitialized());
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
        sizeBatchingStrategy = new SizeBatchingStrategy(BATCH_SIZE, new TagBasedPersistenceStrategy(AD_TAG, inMemoryPersistenceStrategy));
        timeBatchingStrategy = new TimeBatchingStrategy(TIME_OUT, new TagBasedPersistenceStrategy(DEBUG_TAG, sqlPersistenceStrategy));
//        comboBatchingStrategy = new ComboBatchingStrategy(
//                new SizeBatchingStrategy(BATCH_SIZE, new TagBasedPersistenceStrategy(BUSINESS_TAG, sqlPersistenceStrategy))
//                , new TimeBatchingStrategy(TIME_OUT, new TagBasedPersistenceStrategy(BUSINESS_TAG, sqlPersistenceStrategy)));
//        //Add tag strategy
        tagBatchingStrategy.addTagStrategy(AD_TAG, sizeBatchingStrategy);
        tagBatchingStrategy.addTagStrategy(DEBUG_TAG, timeBatchingStrategy);

        tagBatchingStrategy.onInitialized(batchController, context, onBatchReadyListener, handler);
    }
}
