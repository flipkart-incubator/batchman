package com.flipkart.batching.strategy;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.flipkart.Utils;
import com.flipkart.batching.Batch;
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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by anirudh.r on 13/02/16.
 * Test for {@link TagBatchingStrategy}
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class TagBatchingTest {

    /**
     * Test for{@link TagBatchingStrategy#onDataPushed(Collection)}
     */
    @Test
    public void testOnDataPushed() {
        int BATCH_SIZE = 5;
        long TIME_OUT = 5000;
        Tag AD_TAG = new Tag("ADS");
        Tag DEBUG_TAG = new Tag("DEBUG");

        Context context = RuntimeEnvironment.application;
        InMemoryPersistenceStrategy inMemoryPersistenceStrategy = mock(InMemoryPersistenceStrategy.class);
        SQLPersistenceStrategy sqlPersistenceStrategy = mock(SQLPersistenceStrategy.class);
        OnBatchReadyListener onBatchReadyListener = mock(OnBatchReadyListener.class);
        TagBatchingStrategy tagBatchingStrategy = new TagBatchingStrategy<>();
        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper);

        BatchingStrategy sizeBatchingStrategy = new SizeBatchingStrategy(BATCH_SIZE, new TagBasedPersistenceStrategy<>(AD_TAG, inMemoryPersistenceStrategy));
        BatchingStrategy timeBatchingStrategy = new TimeBatchingStrategy(TIME_OUT, new TagBasedPersistenceStrategy<>(DEBUG_TAG, sqlPersistenceStrategy));

        tagBatchingStrategy.addTagStrategy(AD_TAG, sizeBatchingStrategy);
        tagBatchingStrategy.addTagStrategy(DEBUG_TAG, timeBatchingStrategy);
        tagBatchingStrategy.onInitialized(context, onBatchReadyListener, handler);
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
        int BATCH_SIZE = 5;
        long TIME_OUT = 5000;
        Tag AD_TAG = new Tag("ADS");
        Tag DEBUG_TAG = new Tag("DEBUG");
        Context context = RuntimeEnvironment.application;
        InMemoryPersistenceStrategy inMemoryPersistenceStrategy = mock(InMemoryPersistenceStrategy.class);
        SQLPersistenceStrategy sqlPersistenceStrategy = mock(SQLPersistenceStrategy.class);
        OnBatchReadyListener onBatchReadyListener = mock(OnBatchReadyListener.class);
        TagBatchingStrategy tagBatchingStrategy = new TagBatchingStrategy<>();
        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper);

        BatchingStrategy sizeBatchingStrategy = new SizeBatchingStrategy(BATCH_SIZE, new TagBasedPersistenceStrategy<>(AD_TAG, inMemoryPersistenceStrategy));
        BatchingStrategy timeBatchingStrategy = new TimeBatchingStrategy(TIME_OUT, new TagBasedPersistenceStrategy<>(DEBUG_TAG, sqlPersistenceStrategy));
        tagBatchingStrategy.addTagStrategy(AD_TAG, sizeBatchingStrategy);
        tagBatchingStrategy.addTagStrategy(DEBUG_TAG, timeBatchingStrategy);
        tagBatchingStrategy.onInitialized(context, onBatchReadyListener, handler);

        ArrayList<TagData> adsDataArrayList = Utils.fakeTagAdsCollection(2);
        ArrayList<TagData> debugDataArrayList = Utils.fakeTagDebugCollection(2);
        ArrayList<TagData> arrayList = new ArrayList<>();
        arrayList.addAll(adsDataArrayList);
        arrayList.addAll(debugDataArrayList);

        tagBatchingStrategy.onDataPushed(arrayList);
        when(inMemoryPersistenceStrategy.getData()).thenReturn(adsDataArrayList);
        when(inMemoryPersistenceStrategy.getDataSize()).thenReturn(adsDataArrayList.size());
        when(sqlPersistenceStrategy.getData()).thenReturn(debugDataArrayList);
        when(sqlPersistenceStrategy.getDataSize()).thenReturn(debugDataArrayList.size());
        tagBatchingStrategy.flush(true);

        //verify that removeData gets called for inMemoryPersistence
        verify(inMemoryPersistenceStrategy, times(1)).removeData(eq(adsDataArrayList));
    }

    /**
     * Test for {@link OnBatchReadyListener#onReady(BatchingStrategy, Batch)} when flush is true
     */
    @Test
    public void testOnReadyCallbackFlushTrue() {
        int BATCH_SIZE = 5;
        long TIME_OUT = 5000;
        Tag AD_TAG = new Tag("ADS");
        Tag DEBUG_TAG = new Tag("DEBUG");

        Context context = RuntimeEnvironment.application;
        InMemoryPersistenceStrategy inMemoryPersistenceStrategy = mock(InMemoryPersistenceStrategy.class);
        SQLPersistenceStrategy sqlPersistenceStrategy = mock(SQLPersistenceStrategy.class);
        OnBatchReadyListener onBatchReadyListener = mock(OnBatchReadyListener.class);
        TagBatchingStrategy tagBatchingStrategy = new TagBatchingStrategy<>();
        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper);
        BatchingStrategy sizeBatchingStrategy = new SizeBatchingStrategy(BATCH_SIZE, new TagBasedPersistenceStrategy<>(AD_TAG, inMemoryPersistenceStrategy));
        BatchingStrategy timeBatchingStrategy = new TimeBatchingStrategy(TIME_OUT, new TagBasedPersistenceStrategy<>(DEBUG_TAG, sqlPersistenceStrategy));
        tagBatchingStrategy.addTagStrategy(AD_TAG, sizeBatchingStrategy);
        tagBatchingStrategy.addTagStrategy(DEBUG_TAG, timeBatchingStrategy);
        tagBatchingStrategy.onInitialized(context, onBatchReadyListener, handler);

        ArrayList<TagData> adsDataArrayList = Utils.fakeTagAdsCollection(2);
        ArrayList<TagData> debugDataArrayList = Utils.fakeTagDebugCollection(2);
        ArrayList<TagData> arrayList = new ArrayList<>();
        arrayList.addAll(adsDataArrayList);
        arrayList.addAll(debugDataArrayList);
        tagBatchingStrategy.onDataPushed(arrayList);
        when(inMemoryPersistenceStrategy.getData()).thenReturn(adsDataArrayList);
        when(inMemoryPersistenceStrategy.getDataSize()).thenReturn(adsDataArrayList.size());
        when(sqlPersistenceStrategy.getData()).thenReturn(debugDataArrayList);
        when(sqlPersistenceStrategy.getDataSize()).thenReturn(debugDataArrayList.size());
        tagBatchingStrategy.flush(true);

        //verify that onReady is called, as flush force is true
        verify(onBatchReadyListener, times(2)).onReady(eq(tagBatchingStrategy), any(TagBatchingStrategy.TagBatch.class));
    }

    /**
     * Test for {@link OnBatchReadyListener#onReady(BatchingStrategy, Batch)} when using SizeBatchingStrategy
     */
    @Test
    public void testOnReadyCallbackDataForSize() {
        int BATCH_SIZE = 5;
        long TIME_OUT = 5000;
        Tag AD_TAG = new Tag("ADS");
        Tag DEBUG_TAG = new Tag("DEBUG");
        Context context = RuntimeEnvironment.application;
        InMemoryPersistenceStrategy inMemoryPersistenceStrategy = mock(InMemoryPersistenceStrategy.class);
        SQLPersistenceStrategy sqlPersistenceStrategy = mock(SQLPersistenceStrategy.class);
        OnBatchReadyListener onBatchReadyListener = mock(OnBatchReadyListener.class);
        TagBatchingStrategy tagBatchingStrategy = new TagBatchingStrategy<>();
        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper);

        BatchingStrategy sizeBatchingStrategy = new SizeBatchingStrategy(BATCH_SIZE, new TagBasedPersistenceStrategy<>(AD_TAG, inMemoryPersistenceStrategy));
        BatchingStrategy timeBatchingStrategy = new TimeBatchingStrategy(TIME_OUT, new TagBasedPersistenceStrategy<>(DEBUG_TAG, sqlPersistenceStrategy));

        tagBatchingStrategy.addTagStrategy(AD_TAG, sizeBatchingStrategy);
        tagBatchingStrategy.addTagStrategy(DEBUG_TAG, timeBatchingStrategy);
        tagBatchingStrategy.onInitialized(context, onBatchReadyListener, handler);

        ArrayList<TagData> adsTagDataList = Utils.fakeTagAdsCollection(5);
        tagBatchingStrategy.onDataPushed(adsTagDataList);
        when(inMemoryPersistenceStrategy.getData()).thenReturn(adsTagDataList);
        when(inMemoryPersistenceStrategy.getDataSize()).thenReturn(adsTagDataList.size());
        tagBatchingStrategy.flush(false);
        verify(onBatchReadyListener, times(1)).onReady(eq(tagBatchingStrategy), any(TagBatchingStrategy.TagBatch.class));
    }

    /**
     * Test for {@link OnBatchReadyListener#onReady(BatchingStrategy, Batch)} when using TimeBatchingStrategy
     */
    @Test
    public void testOnReadyCallbackDataForTime() {
        int BATCH_SIZE = 5;
        long TIME_OUT = 5000;
        Tag AD_TAG = new Tag("ADS");
        Tag DEBUG_TAG = new Tag("DEBUG");
        Context context = RuntimeEnvironment.application;
        InMemoryPersistenceStrategy inMemoryPersistenceStrategy = mock(InMemoryPersistenceStrategy.class);
        SQLPersistenceStrategy sqlPersistenceStrategy = mock(SQLPersistenceStrategy.class);
        OnBatchReadyListener onBatchReadyListener = mock(OnBatchReadyListener.class);
        TagBatchingStrategy tagBatchingStrategy = new TagBatchingStrategy<>();
        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        Handler handler = new Handler(looper);

        BatchingStrategy sizeBatchingStrategy = new SizeBatchingStrategy(BATCH_SIZE, new TagBasedPersistenceStrategy<>(AD_TAG, inMemoryPersistenceStrategy));
        BatchingStrategy timeBatchingStrategy = new TimeBatchingStrategy(TIME_OUT, new TagBasedPersistenceStrategy<>(DEBUG_TAG, sqlPersistenceStrategy));

        tagBatchingStrategy.addTagStrategy(AD_TAG, sizeBatchingStrategy);
        tagBatchingStrategy.addTagStrategy(DEBUG_TAG, timeBatchingStrategy);
        tagBatchingStrategy.onInitialized(context, onBatchReadyListener, handler);

        ArrayList<TagData> debugDataList = Utils.fakeTagDebugCollection(4);
        tagBatchingStrategy.onDataPushed(debugDataList);
        when(sqlPersistenceStrategy.getData()).thenReturn(debugDataList);
        tagBatchingStrategy.flush(false);
        shadowLooper.idle(5000);
        //verify it gets called after 5000ms
        verify(onBatchReadyListener, times(1)).onReady(eq(tagBatchingStrategy), any(TagBatchingStrategy.TagBatch.class));
    }

    /**
     * Test for {@link TagBatchingStrategy#addTagStrategy(Tag, BatchingStrategy)}
     */
    @Test
    public void testAddStrategy() {
        int BATCH_SIZE = 5;
        long TIME_OUT = 5000;
        Tag AD_TAG = new Tag("ADS");
        Tag DEBUG_TAG = new Tag("DEBUG");
        Tag BUSINESS_TAG = new Tag("BUSINESS");
        Context context = RuntimeEnvironment.application;
        InMemoryPersistenceStrategy inMemoryPersistenceStrategy = mock(InMemoryPersistenceStrategy.class);
        SQLPersistenceStrategy sqlPersistenceStrategy = mock(SQLPersistenceStrategy.class);
        OnBatchReadyListener onBatchReadyListener = mock(OnBatchReadyListener.class);
        TagBatchingStrategy tagBatchingStrategy = new TagBatchingStrategy<>();
        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper);

        BatchingStrategy sizeBatchingStrategy = new SizeBatchingStrategy(BATCH_SIZE, new TagBasedPersistenceStrategy<>(AD_TAG, inMemoryPersistenceStrategy));
        BatchingStrategy timeBatchingStrategy = new TimeBatchingStrategy(TIME_OUT, new TagBasedPersistenceStrategy<>(DEBUG_TAG, sqlPersistenceStrategy));

        tagBatchingStrategy.addTagStrategy(AD_TAG, sizeBatchingStrategy);
        tagBatchingStrategy.addTagStrategy(DEBUG_TAG, timeBatchingStrategy);
        tagBatchingStrategy.onInitialized(context, onBatchReadyListener, handler);

        Map<Tag, BatchingStrategy> batchingStrategyMap = new HashMap<>();
        batchingStrategyMap.put(AD_TAG, new SizeBatchingStrategy(5, inMemoryPersistenceStrategy));
        batchingStrategyMap.put(DEBUG_TAG, new SizeBatchingStrategy(5, sqlPersistenceStrategy));
        batchingStrategyMap.put(BUSINESS_TAG, new SizeBatchingStrategy(5, inMemoryPersistenceStrategy));
        Assert.assertTrue(batchingStrategyMap.size() == 3);
    }

    /**
     * Test to verify that {@link TagBatchingStrategy#getTagByStrategy(BatchingStrategy)} is null
     */
    @Test
    public void testNoStrategy() {
        TagBatchingStrategy tagBatchingStrategy = new TagBatchingStrategy();
        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();

        Assert.assertNull(tagBatchingStrategy.getTagByStrategy(null));
    }

    /**
     * Test for {@link TagBatchingStrategy#isInitialized()}
     */
    @Test
    public void testOnInitialized() {
        int BATCH_SIZE = 5;
        long TIME_OUT = 5000;
        Tag AD_TAG = new Tag("ADS");
        Tag DEBUG_TAG = new Tag("DEBUG");
        Context context = RuntimeEnvironment.application;
        InMemoryPersistenceStrategy inMemoryPersistenceStrategy = mock(InMemoryPersistenceStrategy.class);
        SQLPersistenceStrategy sqlPersistenceStrategy = mock(SQLPersistenceStrategy.class);
        OnBatchReadyListener onBatchReadyListener = mock(OnBatchReadyListener.class);
        TagBatchingStrategy tagBatchingStrategy = new TagBatchingStrategy<>();
        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper);

        BatchingStrategy sizeBatchingStrategy = new SizeBatchingStrategy(BATCH_SIZE, new TagBasedPersistenceStrategy<>(AD_TAG, inMemoryPersistenceStrategy));
        BatchingStrategy timeBatchingStrategy = new TimeBatchingStrategy(TIME_OUT, new TagBasedPersistenceStrategy<>(DEBUG_TAG, sqlPersistenceStrategy));

        tagBatchingStrategy.addTagStrategy(AD_TAG, sizeBatchingStrategy);
        tagBatchingStrategy.addTagStrategy(DEBUG_TAG, timeBatchingStrategy);
        tagBatchingStrategy.onInitialized(context, onBatchReadyListener, handler);

        //assert that tagBatchingStrategy is initialized
        Assert.assertTrue(tagBatchingStrategy.isInitialized());
    }

    /**
     * Test for {@link TagBatchingStrategy#equals(Object)} for batch
     */
    @Test
    public void testTagBatchEquals() {
        Tag AD_TAG = new Tag("ADS");
        ArrayList<Data> list1 = Utils.fakeCollection(5);
        ArrayList<Data> list2 = new ArrayList<>(list1);

        TagBatchingStrategy.TagBatch tagBatchInfo = new TagBatchingStrategy.TagBatch(AD_TAG, new SizeBatchingStrategy.SizeBatch<>(list1, 5));
        TagBatchingStrategy.TagBatch tagBatchInfo1 = new TagBatchingStrategy.TagBatch(AD_TAG, new SizeBatchingStrategy.SizeBatch<>(list2, 5));

        Assert.assertNotNull(tagBatchInfo.getTag());
        Assert.assertTrue(tagBatchInfo.equals(tagBatchInfo1));
        Assert.assertTrue(!tagBatchInfo.equals("event1"));
    }

    /**
     * Test for {@link TagBatchingStrategy#equals(Object)} for collection
     */
    @Test
    public void testTagBatchCollection() {
        Tag AD_TAG = new Tag("ADS");
        ArrayList<TagData> tagDatas = Utils.fakeTagAdsCollection(4);
        TagBatchingStrategy.TagBatch tagBatch = new TagBatchingStrategy.TagBatch(AD_TAG, new SizeBatchingStrategy.SizeBatch(tagDatas, 4));
        Assert.assertTrue(tagDatas == tagBatch.getDataCollection());
    }
}
