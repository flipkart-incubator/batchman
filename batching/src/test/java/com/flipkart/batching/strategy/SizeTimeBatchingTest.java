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
import com.flipkart.batching.persistence.PersistenceStrategy;

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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class SizeTimeBatchingTest {

    @Test
    public void testOnDataPushed() {
        long TIME_OUT = 5000;
        int BATCH_SIZE = 5;
        Context context = RuntimeEnvironment.application;
        OnBatchReadyListener onBatchReadyListener = mock(OnBatchReadyListener.class);
        PersistenceStrategy persistenceStrategy = mock(PersistenceStrategy.class);
        SizeTimeBatchingStrategy<Data> sizeTimeBatchingStrategy = new SizeTimeBatchingStrategy<>(persistenceStrategy, BATCH_SIZE, TIME_OUT);
        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper);

        sizeTimeBatchingStrategy.onInitialized(context, onBatchReadyListener, handler);
        ArrayList<Data> dataArrayList = Utils.fakeCollection(5);
        sizeTimeBatchingStrategy.onDataPushed(dataArrayList);
        //verify that add method is called one time only.
        verify(persistenceStrategy, times(1)).add(dataArrayList);
    }

    /**
     * This test is to ensure the working of {@link TimeBatchingStrategy#flush(boolean)}
     * Whenever this method is invoked, {@link PersistenceStrategy#removeData(Collection)} should be called
     */
    @Test
    public void testFlush() {
        long TIME_OUT = 5000;
        int BATCH_SIZE = 5;
        Context context = RuntimeEnvironment.application;
        OnBatchReadyListener onBatchReadyListener = mock(OnBatchReadyListener.class);
        PersistenceStrategy persistenceStrategy = mock(PersistenceStrategy.class);
        SizeTimeBatchingStrategy<Data> sizeTimeBatchingStrategy = new SizeTimeBatchingStrategy<>(persistenceStrategy, BATCH_SIZE, TIME_OUT);
        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        Handler handler = new Handler(looper);
        sizeTimeBatchingStrategy.onInitialized(context, onBatchReadyListener, handler);

        ArrayList<Data> dataList = Utils.fakeCollection(1);
        sizeTimeBatchingStrategy.onDataPushed(dataList);
        when(persistenceStrategy.getData()).thenReturn(dataList);
        when(persistenceStrategy.getDataSize()).thenReturn(dataList.size());
        sizeTimeBatchingStrategy.flush(false);
        //removeData method should not be called
        verify(persistenceStrategy, times(0)).removeData(eq(dataList));

        persistenceStrategy = mock(PersistenceStrategy.class);
        sizeTimeBatchingStrategy = new SizeTimeBatchingStrategy<>(persistenceStrategy, BATCH_SIZE, TIME_OUT);
        sizeTimeBatchingStrategy.onInitialized(context, onBatchReadyListener, handler);
        dataList.clear();
        dataList = Utils.fakeCollection(1);
        sizeTimeBatchingStrategy.onDataPushed(dataList);
        when(persistenceStrategy.getData()).thenReturn(dataList);
        when(persistenceStrategy.getDataSize()).thenReturn(dataList.size());
        sizeTimeBatchingStrategy.flush(false);
        shadowLooper.idle(TIME_OUT);
        //verify removeData method gets called 1 time from TimeBatchingStrategy
        verify(persistenceStrategy, times(1)).removeData(eq(dataList));

        persistenceStrategy = mock(PersistenceStrategy.class);
        sizeTimeBatchingStrategy = new SizeTimeBatchingStrategy<>(persistenceStrategy, BATCH_SIZE, TIME_OUT);
        sizeTimeBatchingStrategy.onInitialized(context, onBatchReadyListener, handler);
        dataList.clear();
        dataList = Utils.fakeCollection(1);
        sizeTimeBatchingStrategy.onDataPushed(dataList);
        when(persistenceStrategy.getData()).thenReturn(dataList);
        when(persistenceStrategy.getDataSize()).thenReturn(dataList.size());
        sizeTimeBatchingStrategy.flush(true);
        //verify removeData method gets called 1 time only.
        verify(persistenceStrategy, times(1)).removeData(eq(dataList));
    }

    /**
     * Test for onReady when flush is false
     */
    @Test
    public void testOnReadyCallbackFlushFalse() {
        long TIME_OUT = 5000;
        int BATCH_SIZE = 5;
        Context context = RuntimeEnvironment.application;
        OnBatchReadyListener onBatchReadyListener = mock(OnBatchReadyListener.class);
        PersistenceStrategy persistenceStrategy = mock(PersistenceStrategy.class);
        SizeTimeBatchingStrategy<Data> sizeTimeBatchingStrategy = new SizeTimeBatchingStrategy<>(persistenceStrategy, BATCH_SIZE, TIME_OUT);

        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        Handler handler = new Handler(looper);
        sizeTimeBatchingStrategy.onInitialized(context, onBatchReadyListener, handler);

        //verify that onReady is called, as flush force is true
        ArrayList<Data> data = Utils.fakeCollection(2);
        sizeTimeBatchingStrategy.onDataPushed(data);
        when(persistenceStrategy.getData()).thenReturn(data);
        when(persistenceStrategy.getDataSize()).thenReturn(data.size());
        sizeTimeBatchingStrategy.flush(false);
        shadowLooper.idle(TIME_OUT);
        //verify onReady is called from TimeBatching as size of data is 2.
        verify(onBatchReadyListener, times(1)).onReady(eq(sizeTimeBatchingStrategy), any(Batch.class));

        data.clear();
        reset(onBatchReadyListener);
        data = Utils.fakeCollection(5);
        sizeTimeBatchingStrategy.onDataPushed(data);
        when(persistenceStrategy.getData()).thenReturn(data);
        when(persistenceStrategy.getDataSize()).thenReturn(data.size());
        sizeTimeBatchingStrategy.flush(false);
        //verify onReady is called as size of data is equal to BATCH_SIZE
        verify(onBatchReadyListener, times(1)).onReady(eq(sizeTimeBatchingStrategy), any(Batch.class));
    }

    /**
     * Test for onReady when flush is true.
     */
    @Test
    public void testOnReadyCallbackFlushTrue() {
        long TIME_OUT = 5000;
        int BATCH_SIZE = 5;
        Context context = RuntimeEnvironment.application;

        OnBatchReadyListener onBatchReadyListener = mock(OnBatchReadyListener.class);
        PersistenceStrategy persistenceStrategy = mock(PersistenceStrategy.class);
        SizeTimeBatchingStrategy<Data> sizeTimeBatchingStrategy = new SizeTimeBatchingStrategy<>(persistenceStrategy, BATCH_SIZE, TIME_OUT);
        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        Handler handler = new Handler(looper);
        sizeTimeBatchingStrategy.onInitialized(context, onBatchReadyListener, handler);

        //verify that onReady is called, as flush force is true
        ArrayList<Data> data = Utils.fakeCollection(2);
        sizeTimeBatchingStrategy.onDataPushed(data);
        when(persistenceStrategy.getData()).thenReturn(data);
        when(persistenceStrategy.getDataSize()).thenReturn(data.size());
        sizeTimeBatchingStrategy.flush(true);
        //verify onReady is called from TimeBatching and SizeBatching as flush force is true
        verify(onBatchReadyListener, atLeastOnce()).onReady(any(BatchingStrategy.class), any(Batch.class));

        data.clear();
        data = Utils.fakeCollection(6);
        sizeTimeBatchingStrategy.onDataPushed(data);
        when(persistenceStrategy.getData()).thenReturn(data);
        when(persistenceStrategy.getDataSize()).thenReturn(data.size());
        sizeTimeBatchingStrategy.flush(true);
        //verify onReady is called from TimeBatching and SizeBatching as flush force is true
        verify(onBatchReadyListener, atLeastOnce()).onReady(any(BatchingStrategy.class), any(Batch.class));
    }

    /**
     * Test for {@link BatchingStrategy#isInitialized()}
     */
    @Test
    public void testOnInitialized() {
        long TIME_OUT = 5000;
        int BATCH_SIZE = 5;
        Context context = RuntimeEnvironment.application;
        BatchController<Data, Batch<Data>> controller = mock(BatchController.class);
        OnBatchReadyListener onBatchReadyListener = mock(OnBatchReadyListener.class);
        PersistenceStrategy persistenceStrategy = mock(PersistenceStrategy.class);
        SizeTimeBatchingStrategy<Data> sizeTimeBatchingStrategy = new SizeTimeBatchingStrategy<>(persistenceStrategy, BATCH_SIZE, TIME_OUT);
        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        Handler handler = new Handler(looper);

        sizeTimeBatchingStrategy.onInitialized(context, onBatchReadyListener, handler);
        Assert.assertTrue(sizeTimeBatchingStrategy.isInitialized());
    }

    @Test
    public void testComboBatchInfo() {
        ArrayList<Data> list1 = Utils.fakeCollection(5);
        ArrayList<Data> list2 = new ArrayList<>(list1);
        SizeTimeBatchingStrategy.SizeTimeBatch sizeTimeBatch = new SizeTimeBatchingStrategy.SizeTimeBatch(list1, 5, 5000);
        SizeTimeBatchingStrategy.SizeTimeBatch sizeTimeBatch1 = new SizeTimeBatchingStrategy.SizeTimeBatch(list2, 5, 5000);

        Assert.assertTrue(sizeTimeBatch.equals(sizeTimeBatch1));
        Assert.assertTrue(!sizeTimeBatch.equals("event"));
    }
}
