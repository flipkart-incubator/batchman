package com.flipkart.batching.strategy;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.flipkart.Utils;
import com.flipkart.batching.Batch;
import com.flipkart.batching.BatchController;
import com.flipkart.batching.BatchingStrategy;
import com.flipkart.batching.BuildConfig;
import com.flipkart.batching.Data;
import com.flipkart.batching.OnBatchReadyListener;
import com.flipkart.batching.data.Tag;
import com.flipkart.batching.data.TagData;
import com.flipkart.batching.persistence.InMemoryPersistenceStrategy;
import com.flipkart.batching.persistence.SQLPersistenceStrategy;
import com.flipkart.batching.persistence.TagBasedPersistenceStrategy;

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
    private Tag AD_TAG = new Tag("ADS");
    private Tag DEBUG_TAG = new Tag("DEBUG");
    private Tag BUSINESS_TAG = new Tag("BUSINESS");
    private BatchingStrategy<TagData, Batch<TagData>> sizeBatchingStrategy;
    private BatchingStrategy<TagData, Batch<TagData>> timeBatchingStrategy;
    private TagBatchingStrategy<TagData> tagBatchingStrategy;
    private ShadowLooper shadowLooper;
    @Mock
    private InMemoryPersistenceStrategy<TagData> inMemoryPersistenceStrategy;
    @Mock
    private SQLPersistenceStrategy<TagData> sqlPersistenceStrategy;
    @Mock
    private BatchController<TagData, Batch<TagData>> batchController;
    @Mock
    private Context context;
    @Mock
    private OnBatchReadyListener<TagData, TagBatchingStrategy.TagBatch<TagData>> onBatchReadyListener;

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
        ArrayList<TagData> adsDataArrayList = Utils.fakeTagAdsCollection(4);
        ArrayList<TagData> debugDataArrayList = Utils.fakeTagDebugCollection(4);
        ArrayList<TagData> arrayList = new ArrayList<>();
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
     * {@link com.flipkart.batching.persistence.PersistenceStrategy#removeData(Collection)} method
     */
    @Test
    public void testFlush() {
        initializeTagBatching();
        ArrayList<TagData> adsDataArrayList = Utils.fakeTagAdsCollection(2);
        ArrayList<TagData> debugDataArrayList = Utils.fakeTagDebugCollection(2);
        ArrayList<TagData> arrayList = new ArrayList<>();
        arrayList.addAll(adsDataArrayList);
        arrayList.addAll(debugDataArrayList);

        tagBatchingStrategy.onDataPushed(arrayList);
        when(inMemoryPersistenceStrategy.getData()).thenReturn(adsDataArrayList);
        when(sqlPersistenceStrategy.getData()).thenReturn(debugDataArrayList);
        tagBatchingStrategy.flush(true);

        verify(inMemoryPersistenceStrategy, times(1)).removeData(eq(adsDataArrayList));
    }

    @Test
    public void testOnReadyCallbackFlushTrue() {
        initializeTagBatching();
        //verify that onReady is called, as flush force is true
        ArrayList<TagData> adsDataArrayList = Utils.fakeTagAdsCollection(2);
        ArrayList<TagData> debugDataArrayList = Utils.fakeTagDebugCollection(2);
        ArrayList<TagData> arrayList = new ArrayList<>();
        arrayList.addAll(adsDataArrayList);
        arrayList.addAll(debugDataArrayList);
        tagBatchingStrategy.onDataPushed(arrayList);
        when(inMemoryPersistenceStrategy.getData()).thenReturn(adsDataArrayList);
        when(sqlPersistenceStrategy.getData()).thenReturn(debugDataArrayList);
        tagBatchingStrategy.flush(true);

        verify(onBatchReadyListener, times(2)).onReady(eq(tagBatchingStrategy), any(TagBatchingStrategy.TagBatch.class));
    }

    @Test
    public void testOnReadyCallbackDataForSize() {
        initializeTagBatching();
        ArrayList<TagData> adsTagDataList = Utils.fakeTagAdsCollection(5);
        tagBatchingStrategy.onDataPushed(adsTagDataList);
        when(inMemoryPersistenceStrategy.getData()).thenReturn(adsTagDataList);
        tagBatchingStrategy.flush(false);
        verify(onBatchReadyListener, times(1)).onReady(eq(tagBatchingStrategy), any(TagBatchingStrategy.TagBatch.class));
    }

    @Test
    public void testOnReadyCallbackDataForTime() {
        initializeTagBatching();
        ArrayList<TagData> debugDataList = Utils.fakeTagDebugCollection(4);
        tagBatchingStrategy.onDataPushed(debugDataList);
        when(sqlPersistenceStrategy.getData()).thenReturn(debugDataList);
        tagBatchingStrategy.flush(false);
        shadowLooper.idle(5000);
        verify(onBatchReadyListener, times(1)).onReady(eq(tagBatchingStrategy), any(TagBatchingStrategy.TagBatch.class));
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
    public void testNoStrategy() {
        tagBatchingStrategy = new TagBatchingStrategy();
        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        shadowLooper = Shadows.shadowOf(looper);
        Handler handler = new Handler(looper);

        Assert.assertNull(tagBatchingStrategy.getTagByStrategy(null));

    }

    @Test
    public void testOnInitialized() {
        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper);
        TagBatchingStrategy tagBatchingStrategy = new TagBatchingStrategy();
        tagBatchingStrategy.onInitialized(context, onBatchReadyListener, handler);

        Assert.assertTrue(tagBatchingStrategy.isInitialized());
    }

    /**
     * Initialize the TagBatchingStrategy
     */
    private void initializeTagBatching() {
        tagBatchingStrategy = new TagBatchingStrategy<>();
        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        shadowLooper = Shadows.shadowOf(looper);
        Handler handler = new Handler(looper);

        sizeBatchingStrategy = new SizeBatchingStrategy(BATCH_SIZE, new TagBasedPersistenceStrategy<>(AD_TAG, inMemoryPersistenceStrategy));
        timeBatchingStrategy = new TimeBatchingStrategy(TIME_OUT, new TagBasedPersistenceStrategy<>(DEBUG_TAG, sqlPersistenceStrategy));

        tagBatchingStrategy.addTagStrategy(AD_TAG, sizeBatchingStrategy);
        tagBatchingStrategy.addTagStrategy(DEBUG_TAG, timeBatchingStrategy);
        tagBatchingStrategy.onInitialized(context, onBatchReadyListener, handler);
    }

    @Test
    public void testTagBatchingInfo() {

        ArrayList<Data> list1 = Utils.fakeCollection(5);
        ArrayList<Data> list2 = new ArrayList<>(list1);

        TagBatchingStrategy.TagBatch tagBatchInfo = new TagBatchingStrategy.TagBatch(AD_TAG, new SizeBatchingStrategy.SizeBatch<>(list1, 5));
        TagBatchingStrategy.TagBatch tagBatchInfo1 = new TagBatchingStrategy.TagBatch(AD_TAG, new SizeBatchingStrategy.SizeBatch<>(list2, 5));

        Assert.assertNotNull(tagBatchInfo.getTag());
        Assert.assertTrue(tagBatchInfo.equals(tagBatchInfo1));
        Assert.assertTrue(!tagBatchInfo.equals("event1"));
    }

    @Test
    public void testTagBatchCollection() {
        ArrayList<TagData> tagDatas = Utils.fakeTagAdsCollection(4);
        TagBatchingStrategy.TagBatch tagBatch = new TagBatchingStrategy.TagBatch(AD_TAG, new SizeBatchingStrategy.SizeBatch(tagDatas, 4));
        Assert.assertTrue(tagDatas == tagBatch.getDataCollection());
    }
}
