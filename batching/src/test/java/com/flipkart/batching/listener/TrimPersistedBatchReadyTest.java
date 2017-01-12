/*
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2016 Flipkart Internet Pvt. Ltd.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package com.flipkart.batching.listener;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.flipkart.Utils;
import com.flipkart.batching.BaseTestClass;
import com.flipkart.batching.BatchManager;
import com.flipkart.batching.BuildConfig;
import com.flipkart.batching.strategy.SizeBatchingStrategy;
import com.flipkart.batching.gson.GsonSerializationStrategy;
import com.flipkart.batching.core.Batch;
import com.flipkart.batching.core.Data;
import com.flipkart.batching.core.batch.SizeBatch;
import com.flipkart.batching.core.exception.SerializeException;

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
 * Test for {@link TrimPersistedBatchReadyListener}
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class TrimPersistedBatchReadyTest extends BaseTestClass {


    /**
     * Test to verify {@link TrimmedBatchCallback#onTrimmed(int, int)} is called
     *
     * @throws IOException
     * @throws SerializeException
     */
    @Test
    public void testOnTrimmedCalled() throws IOException {
        int MAX_QUEUE_SIZE = 3;
        int TRIM_TO_SIZE = 1;

        File file = createRandomFile();
        GsonSerializationStrategy<Data, Batch<Data>> serializationStrategy = new GsonSerializationStrategy<>( );
        serializationStrategy.build();
        final ArrayList<Data> dataList = Utils.fakeAdsCollection(10);

        HandlerThread handlerThread = new HandlerThread(createRandomString());
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        Handler handler = new Handler(looper);

        TrimmedBatchCallback trimmedBatchCallback = mock(TrimmedBatchCallback.class);
        SizeBatchingStrategy sizeBatchingStrategy = mock(SizeBatchingStrategy.class);
        SizeBatch<Data> sizeBatch = new SizeBatch<>(dataList, 3);

        TrimPersistedBatchReadyListener<Data, Batch<Data>> trimPersistedBatchReadyListener = new TrimPersistedBatchReadyListener<Data, Batch<Data>>(createRandomFile().getPath(), serializationStrategy,
                handler, MAX_QUEUE_SIZE, TRIM_TO_SIZE, TrimPersistedBatchReadyListener.MODE_TRIM_AT_START | TrimPersistedBatchReadyListener.MODE_TRIM_ON_READY, null, trimmedBatchCallback);

        trimPersistedBatchReadyListener.onReady(sizeBatchingStrategy, sizeBatch);
        trimPersistedBatchReadyListener.onReady(sizeBatchingStrategy, sizeBatch);
        trimPersistedBatchReadyListener.onReady(sizeBatchingStrategy, sizeBatch);
        shadowLooper.runToEndOfTasks();
        //verify that onTrimmed gets called once
        verify(trimmedBatchCallback, times(1)).onTrimmed(MAX_QUEUE_SIZE, MAX_QUEUE_SIZE - TRIM_TO_SIZE);
    }

    /**
     * Test to verify {@link TrimmedBatchCallback#onTrimmed(int, int)} is not called
     *
     * @throws IOException
     * @throws SerializeException
     */
    @Test
    public void testTrimmedNotCalled() throws IOException {
        int MAX_QUEUE_SIZE = 3;
        int TRIM_TO_SIZE = 1;

        File file = createRandomFile();
        GsonSerializationStrategy<Data, Batch<Data>> serializationStrategy = new GsonSerializationStrategy<>( );
        serializationStrategy.build();
        final ArrayList<Data> dataList = Utils.fakeAdsCollection(10);

        HandlerThread handlerThread = new HandlerThread(createRandomString());
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        Handler handler = new Handler(looper);

        TrimmedBatchCallback trimmedBatchCallback = mock(TrimmedBatchCallback.class);
        SizeBatchingStrategy sizeBatchingStrategy = mock(SizeBatchingStrategy.class);
        SizeBatch<Data> sizeBatch = new SizeBatch<>(dataList, 3);

        TrimPersistedBatchReadyListener<Data, Batch<Data>> trimPersistedBatchReadyListener = new TrimPersistedBatchReadyListener<Data, Batch<Data>>(createRandomFile().getPath(), serializationStrategy,
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
    public void testTrimMode() throws IOException {
        int MAX_QUEUE_SIZE = 3;
        int TRIM_TO_SIZE = 1;

        File file = createRandomFile();
        GsonSerializationStrategy<Data, Batch<Data>> serializationStrategy = new GsonSerializationStrategy<>( );
        serializationStrategy.build();
        final ArrayList<Data> dataList = Utils.fakeAdsCollection(10);

        HandlerThread handlerThread = new HandlerThread(createRandomString());
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        Handler handler = new Handler(looper);
        String filePath = createRandomFile().getPath();

        TrimmedBatchCallback trimmedBatchCallback = mock(TrimmedBatchCallback.class);
        SizeBatchingStrategy sizeBatchingStrategy = mock(SizeBatchingStrategy.class);
        SizeBatch<Data> sizeBatch = new SizeBatch<>(dataList, 3);

        TrimPersistedBatchReadyListener<Data, Batch<Data>> trimPersistedBatchReadyListener = new TrimPersistedBatchReadyListener<Data, Batch<Data>>(filePath, serializationStrategy,
                handler, MAX_QUEUE_SIZE, TRIM_TO_SIZE, TrimPersistedBatchReadyListener.MODE_TRIM_AT_START, null, trimmedBatchCallback);

        trimPersistedBatchReadyListener.onReady(sizeBatchingStrategy, sizeBatch);
        trimPersistedBatchReadyListener.onReady(sizeBatchingStrategy, sizeBatch);
        trimPersistedBatchReadyListener.onReady(sizeBatchingStrategy, sizeBatch);
        shadowLooper.runToEndOfTasks();
        //verify that onTrimmed does not gets called
        verify(trimmedBatchCallback, times(0)).onTrimmed(MAX_QUEUE_SIZE, TRIM_TO_SIZE);

        trimPersistedBatchReadyListener = new TrimPersistedBatchReadyListener<Data, Batch<Data>>(filePath, serializationStrategy,
                handler, MAX_QUEUE_SIZE, TRIM_TO_SIZE, TrimPersistedBatchReadyListener.MODE_TRIM_AT_START, null, trimmedBatchCallback);
        SizeBatch<Data> sizeBatch2 = new SizeBatch<>(dataList, 1);
        trimPersistedBatchReadyListener.onReady(sizeBatchingStrategy, sizeBatch2);
        shadowLooper.runToEndOfTasks();
        //verify that onTrimmed gets called
        verify(trimmedBatchCallback, times(1)).onTrimmed(MAX_QUEUE_SIZE, MAX_QUEUE_SIZE - TRIM_TO_SIZE);
    }

    /**
     * Test to verify that exception is thrown
     * when {@link TrimPersistedBatchReadyListener#trimSize} > {@link TrimPersistedBatchReadyListener#maxQueueSize}
     */
    @Test(expected = IllegalArgumentException.class)
    public void testThrowException() {
        int MAX_QUEUE_SIZE = 3;

        File file = createRandomFile();
        GsonSerializationStrategy<Data, Batch<Data>> serializationStrategy = new GsonSerializationStrategy<>( );
        serializationStrategy.build();
        final ArrayList<Data> dataList = Utils.fakeAdsCollection(10);

        HandlerThread handlerThread = new HandlerThread(createRandomString());
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        Handler handler = new Handler(looper);

        TrimmedBatchCallback trimmedBatchCallback = mock(TrimmedBatchCallback.class);

        //throw exception as TrimToSize is greater than MaxQueueSize
        TrimPersistedBatchReadyListener<Data, Batch<Data>> trimPersistedBatchReadyListener = new TrimPersistedBatchReadyListener<Data, Batch<Data>>(createRandomFile().getPath(), serializationStrategy,
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
