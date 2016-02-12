package com.flipkart.batching;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.flipkart.Utils;
import com.flipkart.data.Data;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by anirudh.r on 11/02/16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class TimeBatchingTest {

    @Mock
    private PersistenceStrategy persistenceStrategy;
    @Mock
    private Data eventData;
    @Mock
    private Context context;
    @Mock
    private BatchController controller;
    @Mock
    private OnBatchReadyListener onBatchReadyListener;

    private long TIME_OUT = 5000;
    private ShadowLooper shadowLooper;
    private TimeBatchingStrategy timeBatchingStrategy;

    /**
     * Setting up the test environment.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * This test is to ensure the working of {@link TimeBatchingStrategy#onDataPushed(Collection)} )
     */
    @Test
    public void testOnDataPushed() {
        initializeTimeBatchingStrategy();
        ArrayList<Data> dataList = Utils.fakeCollection(1);
        timeBatchingStrategy.onDataPushed(dataList);
        verify(persistenceStrategy, times(1)).add(dataList);
    }

    /**
     * This test is to ensure the working of {@link TimeBatchingStrategy#flush(boolean)}
     * Whenever this method is invoked, {@link com.flipkart.persistence.PersistenceStrategy#removeData(Collection)} should be called
     */
    @Test
    public void testFlush() {
        initializeTimeBatchingStrategy();
        ArrayList<Data> dataList = Utils.fakeCollection(1);
        timeBatchingStrategy.onDataPushed(dataList);
        when(persistenceStrategy.getData()).thenReturn(dataList);
        timeBatchingStrategy.flush(true);
        verify(persistenceStrategy, times(1)).removeData(eq(dataList));
    }

    /**
     * This test is to check the {@link OnBatchReadyListener#onReady(Collection)} callback for various uses cases.
     * Flush is TRUE for this test. {@link OnBatchReadyListener#onReady(Collection)} should be called every time.
     */
    @Test
    public void testOnReadyCallbackFlushTrue() {
        initializeTimeBatchingStrategy();

        //verify that onReady is called, as flush force is true
        ArrayList<Data> data = Utils.fakeCollection(2);
        timeBatchingStrategy.onDataPushed(data);
        when(persistenceStrategy.getData()).thenReturn(data);
        timeBatchingStrategy.flush(true);
        verify(onBatchReadyListener, times(1)).onReady(eq(data));

        data.clear();
        data = Utils.fakeCollection(5);
        timeBatchingStrategy.onDataPushed(data);
        when(persistenceStrategy.getData()).thenReturn(data);
        timeBatchingStrategy.flush(true);
        verify(onBatchReadyListener, times(1)).onReady(eq(data));
    }

    /**
     * This test is to check the {@link OnBatchReadyListener#onReady(Collection)} callback.
     * This test ensures the integrity of the data.
     */
    @Test
    public void testOnReadyCallbackData() {
        initializeTimeBatchingStrategy();
        ArrayList<Data> data = Utils.fakeCollection(5);
        timeBatchingStrategy.onDataPushed(data);
        when(persistenceStrategy.getData()).thenReturn(data);
        timeBatchingStrategy.flush(false);
        shadowLooper.idle(TIME_OUT);
        verify(onBatchReadyListener, times(1)).onReady(eq(data));

        reset(onBatchReadyListener);
        data.clear();
        List<Data> singleData = Collections.singletonList(eventData);
        data.addAll(singleData);
        timeBatchingStrategy.onDataPushed(data);
        when(persistenceStrategy.getData()).thenReturn(data);
        timeBatchingStrategy.flush(false);
        shadowLooper.idle(TIME_OUT);
        verify(onBatchReadyListener, times(1)).onReady(eq(data));
    }

    /**
     * This test to check the {@link OnBatchReadyListener#onReady(Collection)} callback when the
     * data list passed is empty
     */
    @Test
    public void testOnReadyForEmptyData() {
        initializeTimeBatchingStrategy();
        ArrayList<Data> data = new ArrayList<>();
        timeBatchingStrategy.onDataPushed(data);
        when(persistenceStrategy.getData()).thenReturn(data);
        timeBatchingStrategy.flush(false);
        //verify that onReady is NOT called since the data list is empty.
        verify(onBatchReadyListener, times(0)).onReady(data);
    }

    /**
     * This test is to check if it throws an exception whenever the time out is 0.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIfTimeOutIsZero() {
        new TimeBatchingStrategy(0, persistenceStrategy);
    }

    /**
     * This test is to check if it throws an exception whenever the time out is less than 0.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIfTimeOutNegative() {
        new TimeBatchingStrategy(-4000, persistenceStrategy);
    }

    /**
     * This test is to throw an exception whenever the persistence strategy is null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIfPersistenceNull() {
        new TimeBatchingStrategy(5000, null);
    }

    /**
     * Initialize the {@link TimeBatchingStrategy}.
     */
    private void initializeTimeBatchingStrategy() {
        timeBatchingStrategy = new TimeBatchingStrategy(TIME_OUT, persistenceStrategy);
        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        shadowLooper = Shadows.shadowOf(looper);
        Handler handler = new Handler(looper);
        timeBatchingStrategy.onInitialized(controller, context, onBatchReadyListener, handler);
    }
}
