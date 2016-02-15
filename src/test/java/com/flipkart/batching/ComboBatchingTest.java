package com.flipkart.batching;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.flipkart.Utils;
import com.flipkart.data.Data;
import com.flipkart.persistence.PersistenceStrategy;

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

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by anirudh.r on 13/02/16.
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class ComboBatchingTest {
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
    private int BACTH_SIZE = 5;
    private ShadowLooper shadowLooper;
    private TimeBatchingStrategy timeBatchingStrategy;
    private SizeBatchingStrategy sizeBatchingStrategy;
    private ComboBatchingStrategy comboBatchingStrategy;

    /**
     * Setting up the test environment.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test to verify the working of {@link ComboBatchingStrategy#onDataPushed(Collection)}
     */
    @Test
    public void testOnDataPushed() {
        initializeComboBatching();
        ArrayList<Data> dataArrayList = Utils.fakeCollection(5);
        comboBatchingStrategy.onDataPushed(dataArrayList);
        //verify that add method is called two times, one for size and one for time batching strategy
        verify(persistenceStrategy, times(2)).add(dataArrayList);
    }

    /**
     * This test is to ensure the working of {@link TimeBatchingStrategy#flush(boolean)}
     * Whenever this method is invoked, {@link com.flipkart.persistence.PersistenceStrategy#removeData(Collection)} should be called
     */
    @Test
    public void testFlush() {
        initializeComboBatching();
        ArrayList<Data> dataList = Utils.fakeCollection(1);
        comboBatchingStrategy.onDataPushed(dataList);
        when(persistenceStrategy.getData()).thenReturn(dataList);
        comboBatchingStrategy.flush(false);
        //removeData method should not be called
        verify(persistenceStrategy, times(0)).removeData(eq(dataList));


        initializeComboBatching();
        dataList.clear();
        dataList = Utils.fakeCollection(1);
        comboBatchingStrategy.onDataPushed(dataList);
        when(persistenceStrategy.getData()).thenReturn(dataList);
        comboBatchingStrategy.flush(false);
        shadowLooper.idle(TIME_OUT);
        //verify removeData method gets called 1 time from TimeBatchingStrategy
        verify(persistenceStrategy, times(1)).removeData(eq(dataList));

        initializeComboBatching();
        dataList.clear();
        dataList = Utils.fakeCollection(1);
        comboBatchingStrategy.onDataPushed(dataList);
        when(persistenceStrategy.getData()).thenReturn(dataList);
        comboBatchingStrategy.flush(true);
        //verify removeData method gets called 2 time, one from Time and one from SizeBatchingStrategy
        verify(persistenceStrategy, times(2)).removeData(eq(dataList));
    }

    /**
     * This test is to check the {@link OnBatchReadyListener#onReady(Collection)} callback for various uses cases.
     * Flush is False for this test. {@link OnBatchReadyListener#onReady(Collection)} should be called every time.
     */
    @Test
    public void testOnReadyCallbackFlushFalse() {
        initializeComboBatching();

        //verify that onReady is called, as flush force is true
        ArrayList<Data> data = Utils.fakeCollection(2);
        comboBatchingStrategy.onDataPushed(data);
        when(persistenceStrategy.getData()).thenReturn(data);
        comboBatchingStrategy.flush(false);
        shadowLooper.idle(TIME_OUT);
        //verify onReady is called from TimeBatching as size of data is 2.
        verify(onBatchReadyListener, times(1)).onReady(eq(data));

        data.clear();
        data = Utils.fakeCollection(5);
        comboBatchingStrategy.onDataPushed(data);
        when(persistenceStrategy.getData()).thenReturn(data);
        comboBatchingStrategy.flush(false);
        //verify onReady is called as size of data is equal to BATCH_SIZE
        verify(onBatchReadyListener, times(1)).onReady(eq(data));
    }

    /**
     * This test is to check the {@link OnBatchReadyListener#onReady(Collection)} callback for various uses cases.
     * Flush is True for this test. {@link OnBatchReadyListener#onReady(Collection)} should be called every time.
     */
    @Test
    public void testOnReadyCallbackFlushTrue() {
        initializeComboBatching();

        //verify that onReady is called, as flush force is true
        ArrayList<Data> data = Utils.fakeCollection(2);
        comboBatchingStrategy.onDataPushed(data);
        when(persistenceStrategy.getData()).thenReturn(data);
        comboBatchingStrategy.flush(true);
        //verify onReady is called from TimeBatching and SizeBatching as flush force is true
        verify(onBatchReadyListener, times(2)).onReady(eq(data));

        data.clear();
        data = Utils.fakeCollection(6);
        comboBatchingStrategy.onDataPushed(data);
        when(persistenceStrategy.getData()).thenReturn(data);
        comboBatchingStrategy.flush(true);
        //verify onReady is called from TimeBatching and SizeBatching as flush force is true
        verify(onBatchReadyListener, times(2)).onReady(eq(data));
    }

    /**
     * Test to verify that {@link BatchingStrategy#isInitialized()} is working
     */
    @Test
    public void testOnInitialized() {
        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper);
        ComboBatchingStrategy comboBatchingStrategy = new ComboBatchingStrategy();
        comboBatchingStrategy.onInitialized(controller, context, onBatchReadyListener, handler);

        Assert.assertTrue(comboBatchingStrategy.isInitialized());
    }
    /**
     * Initialize the ComboBatchingStrategy
     */
    private void initializeComboBatching() {
        sizeBatchingStrategy = new SizeBatchingStrategy(BACTH_SIZE, persistenceStrategy);
        timeBatchingStrategy = new TimeBatchingStrategy(TIME_OUT, persistenceStrategy);
        comboBatchingStrategy = new ComboBatchingStrategy(sizeBatchingStrategy, timeBatchingStrategy);
        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        shadowLooper = Shadows.shadowOf(looper);
        Handler handler = new Handler(looper);
        comboBatchingStrategy.onInitialized(controller, context, onBatchReadyListener, handler);
    }

}
