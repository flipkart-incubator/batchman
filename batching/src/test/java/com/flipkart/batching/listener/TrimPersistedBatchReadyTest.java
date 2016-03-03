package com.flipkart.batching.listener;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.flipkart.Utils;
import com.flipkart.batching.BaseTestClass;
import com.flipkart.batching.Batch;
import com.flipkart.batching.BatchManager;
import com.flipkart.batching.BuildConfig;
import com.flipkart.batching.Data;
import com.flipkart.batching.exception.SerializeException;
import com.flipkart.batching.persistence.GsonSerializationStrategy;
import com.flipkart.batching.strategy.SizeBatchingStrategy;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by anirudh.r on 25/02/16.
 * Test for {@link TrimPersistedBatchReadyListener}
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class TrimPersistedBatchReadyTest extends BaseTestClass {


    /**
     * Test to verify {@link TrimmedBatchCallback#onTrimmed(int, int)} is called
     *
     * @throws IOException
     * @throws SerializeException
     */
    @Test
    public void testOnTrimmedCalled() throws IOException, SerializeException {
        int MAX_QUEUE_SIZE = 3;
        int TRIM_TO_SIZE = 1;

        File file = createRandomFile();
        GsonSerializationStrategy<Data, Batch<Data>> serializationStrategy = new GsonSerializationStrategy<>();
        BatchManager.registerBuiltInTypes(serializationStrategy);
        serializationStrategy.build();
        final ArrayList<Data> dataList = Utils.fakeAdsCollection(10);

        HandlerThread handlerThread = new HandlerThread(createRandomString());
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        Handler handler = new Handler(looper);

        TrimmedBatchCallback trimmedBatchCallback = mock(TrimmedBatchCallback.class);
        SizeBatchingStrategy sizeBatchingStrategy = mock(SizeBatchingStrategy.class);
        SizeBatchingStrategy.SizeBatch<Data> sizeBatch = new SizeBatchingStrategy.SizeBatch<>(dataList, 3);

        TrimPersistedBatchReadyListener<Data, Batch<Data>> trimPersistedBatchReadyListener = new TrimPersistedBatchReadyListener<Data, Batch<Data>>(file, serializationStrategy,
                handler, MAX_QUEUE_SIZE, TRIM_TO_SIZE, TrimPersistedBatchReadyListener.MODE_TRIM_AT_START | TrimPersistedBatchReadyListener.MODE_TRIM_ON_READY, null, trimmedBatchCallback);

        trimPersistedBatchReadyListener.onReady(sizeBatchingStrategy, sizeBatch);
        trimPersistedBatchReadyListener.onReady(sizeBatchingStrategy, sizeBatch);
        trimPersistedBatchReadyListener.onReady(sizeBatchingStrategy, sizeBatch);
        shadowLooper.runToEndOfTasks();
        //verify that onTrimmed gets called once
        verify(trimmedBatchCallback, times(1)).onTrimmed(MAX_QUEUE_SIZE, TRIM_TO_SIZE);
    }

    /**
     * Test to verify {@link TrimmedBatchCallback#onTrimmed(int, int)} is not called
     *
     * @throws IOException
     * @throws SerializeException
     */
    @Test
    public void testTrimmedNotCalled() throws IOException, SerializeException {
        int MAX_QUEUE_SIZE = 3;
        int TRIM_TO_SIZE = 1;

        File file = createRandomFile();
        GsonSerializationStrategy<Data, Batch<Data>> serializationStrategy = new GsonSerializationStrategy<>();
        BatchManager.registerBuiltInTypes(serializationStrategy);
        serializationStrategy.build();
        final ArrayList<Data> dataList = Utils.fakeAdsCollection(10);

        HandlerThread handlerThread = new HandlerThread(createRandomString());
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        Handler handler = new Handler(looper);

        TrimmedBatchCallback trimmedBatchCallback = mock(TrimmedBatchCallback.class);
        SizeBatchingStrategy sizeBatchingStrategy = mock(SizeBatchingStrategy.class);
        SizeBatchingStrategy.SizeBatch<Data> sizeBatch = new SizeBatchingStrategy.SizeBatch<>(dataList, 3);

        TrimPersistedBatchReadyListener<Data, Batch<Data>> trimPersistedBatchReadyListener = new TrimPersistedBatchReadyListener<Data, Batch<Data>>(file, serializationStrategy,
                handler, MAX_QUEUE_SIZE, TRIM_TO_SIZE, TrimPersistedBatchReadyListener.MODE_TRIM_AT_START | TrimPersistedBatchReadyListener.MODE_TRIM_ON_READY, null, trimmedBatchCallback);

        trimPersistedBatchReadyListener.onReady(sizeBatchingStrategy, sizeBatch);
        trimPersistedBatchReadyListener.onReady(sizeBatchingStrategy, sizeBatch);
        shadowLooper.runToEndOfTasks();
        //verify that onTrimmed does not gets called.
        verify(trimmedBatchCallback, times(0)).onTrimmed(MAX_QUEUE_SIZE, TRIM_TO_SIZE);
    }

    /**
     * Test to verify {@link TrimPersistedBatchReadyListener#mode}
     *
     * @throws IOException
     * @throws SerializeException
     */
    @Test
    public void testTrimMode() throws IOException, SerializeException {
        int MAX_QUEUE_SIZE = 3;
        int TRIM_TO_SIZE = 1;

        File file = createRandomFile();
        GsonSerializationStrategy<Data, Batch<Data>> serializationStrategy = new GsonSerializationStrategy<>();
        BatchManager.registerBuiltInTypes(serializationStrategy);
        serializationStrategy.build();
        final ArrayList<Data> dataList = Utils.fakeAdsCollection(10);

        HandlerThread handlerThread = new HandlerThread(createRandomString());
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        Handler handler = new Handler(looper);

        TrimmedBatchCallback trimmedBatchCallback = mock(TrimmedBatchCallback.class);
        SizeBatchingStrategy sizeBatchingStrategy = mock(SizeBatchingStrategy.class);
        SizeBatchingStrategy.SizeBatch<Data> sizeBatch = new SizeBatchingStrategy.SizeBatch<>(dataList, 3);

        TrimPersistedBatchReadyListener<Data, Batch<Data>> trimPersistedBatchReadyListener = new TrimPersistedBatchReadyListener<Data, Batch<Data>>(file, serializationStrategy,
                handler, MAX_QUEUE_SIZE, TRIM_TO_SIZE, TrimPersistedBatchReadyListener.MODE_TRIM_AT_START, null, trimmedBatchCallback);

        trimPersistedBatchReadyListener.onReady(sizeBatchingStrategy, sizeBatch);
        trimPersistedBatchReadyListener.onReady(sizeBatchingStrategy, sizeBatch);
        trimPersistedBatchReadyListener.onReady(sizeBatchingStrategy, sizeBatch);
        shadowLooper.runToEndOfTasks();
        //verify that onTrimmed does not gets called
        verify(trimmedBatchCallback, times(0)).onTrimmed(MAX_QUEUE_SIZE, TRIM_TO_SIZE);

        trimPersistedBatchReadyListener = new TrimPersistedBatchReadyListener<Data, Batch<Data>>(file, serializationStrategy,
                handler, MAX_QUEUE_SIZE, TRIM_TO_SIZE, TrimPersistedBatchReadyListener.MODE_TRIM_AT_START, null, trimmedBatchCallback);
        SizeBatchingStrategy.SizeBatch<Data> sizeBatch2 = new SizeBatchingStrategy.SizeBatch<>(dataList, 1);
        trimPersistedBatchReadyListener.onReady(sizeBatchingStrategy, sizeBatch2);
        shadowLooper.runToEndOfTasks();
        //verify that onTrimmed gets called
        verify(trimmedBatchCallback, times(1)).onTrimmed(MAX_QUEUE_SIZE, TRIM_TO_SIZE);
    }

    /**
     * Test to verify that exception is thrown
     * when {@link TrimPersistedBatchReadyListener#trimSize} > {@link TrimPersistedBatchReadyListener#queueSize}
     *
     */
    @Test(expected = IllegalArgumentException.class)
    public void testThrowException() {
        int MAX_QUEUE_SIZE = 3;

        File file = createRandomFile();
        GsonSerializationStrategy<Data, Batch<Data>> serializationStrategy = new GsonSerializationStrategy<>();
        BatchManager.registerBuiltInTypes(serializationStrategy);
        serializationStrategy.build();
        final ArrayList<Data> dataList = Utils.fakeAdsCollection(10);

        HandlerThread handlerThread = new HandlerThread(createRandomString());
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        Handler handler = new Handler(looper);

        TrimmedBatchCallback trimmedBatchCallback = mock(TrimmedBatchCallback.class);

        //throw exception as TrimToSize is greater than MaxQueueSize
        TrimPersistedBatchReadyListener<Data, Batch<Data>> trimPersistedBatchReadyListener = new TrimPersistedBatchReadyListener<Data, Batch<Data>>(file, serializationStrategy,
                handler, MAX_QUEUE_SIZE, 5, TrimPersistedBatchReadyListener.MODE_TRIM_AT_START, null, trimmedBatchCallback);
    }

    /**
     * Delete all test_files after test ends
     */
    @After
    public void afterTest() {
        deleteRandomFiles();
    }

}
