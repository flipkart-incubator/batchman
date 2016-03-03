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
import com.flipkart.batching.listener.PersistedBatchReadyListener;
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
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by anirudh.r on 11/02/16.
 * Test for {@link TimeBatchingStrategy}
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
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
        timeBatchingStrategy.flush(true);
        //verify that it gets called once
        verify(onBatchReadyListener, times(1)).onReady(eq(timeBatchingStrategy), any(TimeBatchingStrategy.TimeBatch.class));

        data.clear();
        data = Utils.fakeCollection(5);
        reset(onBatchReadyListener);
        timeBatchingStrategy.onDataPushed(data);
        when(persistenceStrategy.getData()).thenReturn(data);
        timeBatchingStrategy.flush(true);
        //verify that it gets called once
        verify(onBatchReadyListener, times(1)).onReady(eq(timeBatchingStrategy), any(TimeBatchingStrategy.TimeBatch.class));
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
        timeBatchingStrategy.flush(false);
        shadowLooper.idle(TIME_OUT);
        //verify that it gets called once
        verify(onBatchReadyListener, times(1)).onReady(eq(timeBatchingStrategy), any(TimeBatchingStrategy.TimeBatch.class));

        reset(onBatchReadyListener);
        data.clear();
        List<Data> singleData = Utils.fakeCollection(2);
        data.addAll(singleData);
        timeBatchingStrategy.onDataPushed(data);
        when(persistenceStrategy.getData()).thenReturn(data);
        timeBatchingStrategy.flush(false);
        shadowLooper.idle(TIME_OUT);
        //verify that it gets called once
        verify(onBatchReadyListener, times(1)).onReady(eq(timeBatchingStrategy), any(TimeBatchingStrategy.TimeBatch.class));
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
        timeBatchingStrategy.flush(false);
        //verify that onReady is NOT called since the data list is empty.
        verify(onBatchReadyListener, times(0)).onReady(eq(timeBatchingStrategy), any(TimeBatchingStrategy.TimeBatch.class));
    }

    /**
     * Test for {@link TimeBatchingStrategy#equals(Object)}
     */
    @Test
    public void testTimeBatch() {
        ArrayList<Data> list1 = Utils.fakeCollection(2);
        ArrayList<Data> list2 = new ArrayList<>(list1);
        TimeBatchingStrategy.TimeBatch timeBatchInfo = new TimeBatchingStrategy.TimeBatch<>(list1, 5000);
        TimeBatchingStrategy.TimeBatch timeBatchInfo1 = new TimeBatchingStrategy.TimeBatch<>(list2, 5000);

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
