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
import com.flipkart.batching.BatchingStrategy;
import com.flipkart.batching.BuildConfig;
import com.flipkart.batching.listener.PersistedBatchReadyListener;
import com.flipkart.batching.persistence.PersistenceStrategy;
import com.flipkart.batching.core.Batch;
import com.flipkart.batching.core.Data;
import com.flipkart.batching.core.batch.TimeBatch;

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
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for {@link TimeBatchingStrategy}
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class TimeBatchingTest {

    /**
     * Test for {@link TimeBatchingStrategy#onDataPushed(Collection)} )
     */
    @Test
    public void testOnDataPushed() {
        long TIME_OUT = 5000;

        PersistenceStrategy persistenceStrategy = mock(PersistenceStrategy.class);
        Context context = RuntimeEnvironment.application;
        PersistedBatchReadyListener onBatchReadyListener = mock(PersistedBatchReadyListener.class);
        TimeBatchingStrategy timeBatchingStrategy = new TimeBatchingStrategy(TIME_OUT, persistenceStrategy);
        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper);
        timeBatchingStrategy.onInitialized(context, onBatchReadyListener, handler);

        ArrayList<Data> dataList = Utils.fakeCollection(1);
        timeBatchingStrategy.onDataPushed(dataList);
        //verify it gets called once
        verify(persistenceStrategy, times(1)).add(dataList);
    }

    /**
     * Test for {@link TimeBatchingStrategy#flush(boolean)}
     * Whenever this method is invoked, {@link PersistenceStrategy#removeData(Collection)} should be called
     */
    @Test
    public void testFlush() {
        long TIME_OUT = 5000;

        PersistenceStrategy persistenceStrategy = mock(PersistenceStrategy.class);
        Context context = RuntimeEnvironment.application;
        PersistedBatchReadyListener onBatchReadyListener = mock(PersistedBatchReadyListener.class);
        TimeBatchingStrategy timeBatchingStrategy = new TimeBatchingStrategy(TIME_OUT, persistenceStrategy);
        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper);
        timeBatchingStrategy.onInitialized(context, onBatchReadyListener, handler);
        ArrayList<Data> dataList = Utils.fakeCollection(1);
        timeBatchingStrategy.onDataPushed(dataList);
        when(persistenceStrategy.getData()).thenReturn(dataList);
        when(persistenceStrategy.getDataSize()).thenReturn(dataList.size());
        timeBatchingStrategy.flush(true);
        //verify it gets called once
        verify(persistenceStrategy, times(1)).removeData(eq(dataList));
    }

    /**
     * Test for {@link PersistedBatchReadyListener#onReady(BatchingStrategy, Batch)} when flush is true.
     */
    @Test
    public void testOnReadyCallbackFlushTrue() {
        long TIME_OUT = 5000;

        PersistenceStrategy persistenceStrategy = mock(PersistenceStrategy.class);
        Context context = RuntimeEnvironment.application;
        PersistedBatchReadyListener onBatchReadyListener = mock(PersistedBatchReadyListener.class);
        TimeBatchingStrategy timeBatchingStrategy = new TimeBatchingStrategy(TIME_OUT, persistenceStrategy);
        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper);
        timeBatchingStrategy.onInitialized(context, onBatchReadyListener, handler);
        //verify that onReady is called, as flush force is true
        ArrayList<Data> data = Utils.fakeCollection(2);
        timeBatchingStrategy.onDataPushed(data);
        when(persistenceStrategy.getData()).thenReturn(data);
        when(persistenceStrategy.getDataSize()).thenReturn(data.size());
        timeBatchingStrategy.flush(true);
        //verify that it gets called once
        verify(onBatchReadyListener, times(1)).onReady(eq(timeBatchingStrategy), any(TimeBatch.class));

        data.clear();
        data = Utils.fakeCollection(5);
        reset(onBatchReadyListener);
        timeBatchingStrategy.onDataPushed(data);
        when(persistenceStrategy.getData()).thenReturn(data);
        when(persistenceStrategy.getDataSize()).thenReturn(data.size());
        timeBatchingStrategy.flush(true);
        //verify that it gets called once
        verify(onBatchReadyListener, times(1)).onReady(eq(timeBatchingStrategy), any(TimeBatch.class));
    }

    /**
     * Test for {@link PersistedBatchReadyListener#onReady(BatchingStrategy, Batch)}
     */
    @Test
    public void testOnReadyCallbackData() {
        long TIME_OUT = 5000;
        PersistenceStrategy persistenceStrategy = mock(PersistenceStrategy.class);
        Context context = RuntimeEnvironment.application;
        PersistedBatchReadyListener onBatchReadyListener = mock(PersistedBatchReadyListener.class);
        TimeBatchingStrategy timeBatchingStrategy = new TimeBatchingStrategy(TIME_OUT, persistenceStrategy);
        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        Handler handler = new Handler(looper);
        timeBatchingStrategy.onInitialized(context, onBatchReadyListener, handler);
        ArrayList<Data> data = Utils.fakeCollection(3);
        timeBatchingStrategy.onDataPushed(data);
        when(persistenceStrategy.getData()).thenReturn(data);
        when(persistenceStrategy.getDataSize()).thenReturn(data.size());
        timeBatchingStrategy.flush(false);
        shadowLooper.idle(TIME_OUT);
        //verify that it gets called once
        verify(onBatchReadyListener, times(1)).onReady(eq(timeBatchingStrategy), any(TimeBatch.class));

        reset(onBatchReadyListener);
        data.clear();
        List<Data> singleData = Utils.fakeCollection(2);
        data.addAll(singleData);
        timeBatchingStrategy.onDataPushed(data);
        when(persistenceStrategy.getData()).thenReturn(data);
        when(persistenceStrategy.getDataSize()).thenReturn(data.size());
        timeBatchingStrategy.flush(false);
        shadowLooper.idle(TIME_OUT);
        //verify that it gets called once
        verify(onBatchReadyListener, times(1)).onReady(eq(timeBatchingStrategy), any(TimeBatch.class));
    }

    /**
     * Test for {@link PersistedBatchReadyListener#onReady(BatchingStrategy, Batch)} for empty data
     */
    @Test
    public void testOnReadyForEmptyData() {
        long TIME_OUT = 5000;

        PersistenceStrategy persistenceStrategy = mock(PersistenceStrategy.class);
        Context context = RuntimeEnvironment.application;
        PersistedBatchReadyListener onBatchReadyListener = mock(PersistedBatchReadyListener.class);
        TimeBatchingStrategy timeBatchingStrategy = new TimeBatchingStrategy(TIME_OUT, persistenceStrategy);
        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper);
        timeBatchingStrategy.onInitialized(context, onBatchReadyListener, handler);
        ArrayList<Data> data = new ArrayList<>();
        timeBatchingStrategy.onDataPushed(data);
        when(persistenceStrategy.getData()).thenReturn(data);
        when(persistenceStrategy.getDataSize()).thenReturn(data.size());
        timeBatchingStrategy.flush(false);
        //verify that onReady is NOT called since the data list is empty.
        verify(onBatchReadyListener, times(0)).onReady(eq(timeBatchingStrategy), any(TimeBatch.class));
    }

    /**
     * Test for {@link TimeBatchingStrategy#equals(Object)}
     */
    @Test
    public void testTimeBatch() {
        ArrayList<Data> list1 = Utils.fakeCollection(2);
        ArrayList<Data> list2 = new ArrayList<>(list1);
        TimeBatch timeBatchInfo = new TimeBatch<>(list1, 5000);
        TimeBatch timeBatchInfo1 = new TimeBatch<>(list2, 5000);

        Assert.assertTrue(timeBatchInfo.equals(timeBatchInfo1));
        Assert.assertTrue(!timeBatchInfo.equals("a"));
    }

    /**
     * This test is to check if it throws an exception whenever the time out is 0.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIfTimeOutIsZero() {
        long TIME_OUT = 5000;
        PersistenceStrategy persistenceStrategy = mock(PersistenceStrategy.class);
        Context context = RuntimeEnvironment.application;
        PersistedBatchReadyListener onBatchReadyListener = mock(PersistedBatchReadyListener.class);
        TimeBatchingStrategy timeBatchingStrategy = new TimeBatchingStrategy(TIME_OUT, persistenceStrategy);
        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper);
        timeBatchingStrategy.onInitialized(context, onBatchReadyListener, handler);

        //will throw exception as time is 0
        new TimeBatchingStrategy(0, persistenceStrategy);
    }

    /**
     * This test is to check if it throws an exception whenever the time out is less than 0.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIfTimeOutNegative() {
        long TIME_OUT = 5000;
        PersistenceStrategy persistenceStrategy = mock(PersistenceStrategy.class);
        Context context = RuntimeEnvironment.application;
        PersistedBatchReadyListener onBatchReadyListener = mock(PersistedBatchReadyListener.class);
        TimeBatchingStrategy timeBatchingStrategy = new TimeBatchingStrategy(TIME_OUT, persistenceStrategy);
        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper);
        timeBatchingStrategy.onInitialized(context, onBatchReadyListener, handler);

        //will throw exception as time is negative
        new TimeBatchingStrategy(-4000, persistenceStrategy);
    }

    /**
     * This test is to throw an exception whenever the persistence strategy is null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIfPersistenceNull() {
        //will throw exception as persistence is null
        new TimeBatchingStrategy(5000, null);
    }
}
