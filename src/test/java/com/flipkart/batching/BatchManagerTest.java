package com.flipkart.batching;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.flipkart.Utils;
import com.flipkart.batching.persistence.PersistenceStrategy;
import com.flipkart.batching.persistence.SerializationStrategy;
import com.flipkart.batching.strategy.BaseBatchingStrategy;
import com.flipkart.batching.strategy.SizeBatchingStrategy;

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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by anirudh.r on 12/02/16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class BatchManagerTest {

    @Mock
    SerializationStrategy<Data, Batch<Data>> serializationStrategy;
    @Mock
    PersistenceStrategy<Data> persistenceStrategy;
    @Mock
    OnBatchReadyListener<Data, Batch<Data>> onBatchReadyListener;
    @Mock
    Context context;
    @Mock
    Data eventData;
    ShadowLooper shadowLooper;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void testAddToBatch() {
        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        shadowLooper = Shadows.shadowOf(looper);
        Handler handler = new Handler(looper);
        BaseBatchingStrategy<Data, Batch<Data>> sizeBatchingStrategy = new SizeBatchingStrategy(5, persistenceStrategy);
        BatchManager batchController = new BatchManager.Builder<>()
                .setSerializationStrategy(serializationStrategy)
                .setBatchingStrategy(sizeBatchingStrategy)
                .setHandler(handler)
                .setOnBatchReadyListener(onBatchReadyListener)
                .build(context);

        ArrayList<Data> fakeCollection = Utils.fakeCollection(5);

        when(persistenceStrategy.getData()).thenReturn(fakeCollection);
        batchController.addToBatch(fakeCollection);
        shadowLooper.runToEndOfTasks();
        verify(onBatchReadyListener, times(1)).onReady(eq(sizeBatchingStrategy), any(Batch.class));
    }

    /**
     * Test to verify that handler is not null
     */
    @Test
    public void testHandlerNotNull() {
        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper);
        SizeBatchingStrategy sizeBatchingStrategy = new SizeBatchingStrategy(5, persistenceStrategy);
        BatchController batchController = new BatchManager.Builder()
                .setSerializationStrategy(serializationStrategy)
                .setBatchingStrategy(sizeBatchingStrategy)
                .setHandler(handler)
                .setOnBatchReadyListener(onBatchReadyListener)
                .build(context);

        Assert.assertNotNull(batchController.getHandler());

        batchController = new BatchManager.Builder()
                .setSerializationStrategy(serializationStrategy)
                .setBatchingStrategy(sizeBatchingStrategy)
                .setHandler(null)
                .setOnBatchReadyListener(onBatchReadyListener)
                .build(context);

        Assert.assertNotNull(batchController.getHandler());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBatchingStrategyNullException() {
        BatchController batchController = new BatchManager.Builder()
                .setSerializationStrategy(serializationStrategy)
                .setBatchingStrategy(null)
                .setHandler(null)
                .setOnBatchReadyListener(onBatchReadyListener)
                .build(context);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSerializationStrategyNullException() {
        SizeBatchingStrategy sizeBatchingStrategy = new SizeBatchingStrategy(5, persistenceStrategy);
        BatchController batchController = new BatchManager.Builder()
                .setSerializationStrategy(null)
                .setBatchingStrategy(sizeBatchingStrategy)
                .setHandler(null)
                .setOnBatchReadyListener(onBatchReadyListener)
                .build(context);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOnReadyListenerNullException() {
        SizeBatchingStrategy sizeBatchingStrategy = new SizeBatchingStrategy(5, persistenceStrategy);
        BatchController batchController = new BatchManager.Builder()
                .setSerializationStrategy(serializationStrategy)
                .setBatchingStrategy(sizeBatchingStrategy)
                .setHandler(null)
                .setOnBatchReadyListener(null)
                .build(context);
    }

    @Test
    public void testRegisterSuppliedTypes() {
        SizeBatchingStrategy sizeBatchingStrategy = new SizeBatchingStrategy(5, persistenceStrategy);
        BatchController batchController = new BatchManager.Builder()
                .setSerializationStrategy(serializationStrategy)
                .setBatchingStrategy(sizeBatchingStrategy)
                .setHandler(null)
                .setOnBatchReadyListener(onBatchReadyListener).registerDataType(Data.class)
                .build(context);

        verify(serializationStrategy, times(1)).registerDataType(Data.class);
    }

    @Test
    public void testRegisterBatchInfoType() {
        BaseBatchingStrategy<Data,Batch<Data>> sizeBatchingStrategy = new SizeBatchingStrategy(5, persistenceStrategy);
        new BatchManager.Builder<>()
                .setSerializationStrategy(serializationStrategy)
                .setBatchingStrategy(sizeBatchingStrategy)
                .setHandler(null)
                .setOnBatchReadyListener(onBatchReadyListener).registerBatchInfoType(Batch.class)
                .build(context);

    }

    @Test
    public void testGetSerializationStrategy() {
        SizeBatchingStrategy sizeBatchingStrategy = new SizeBatchingStrategy(5, persistenceStrategy);
        BatchController batchController = new BatchManager.Builder()
                .setSerializationStrategy(serializationStrategy)
                .setBatchingStrategy(sizeBatchingStrategy)
                .setHandler(null)
                .setOnBatchReadyListener(onBatchReadyListener)
                .build(context);

        Assert.assertNotNull(batchController.getSerializationStrategy());
    }

    /**
     * Throw error if {@link BatchingStrategy} is not initialized
     */
    @Test(expected = IllegalAccessError.class)
    public void testIfInitialized() {
        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        shadowLooper = Shadows.shadowOf(looper);
        Handler handler = new Handler(looper);

        BatchingStrategy sizeBatchingStrategy = mock(BatchingStrategy.class);
        when(sizeBatchingStrategy.isInitialized()).thenReturn(false);
        BatchController batchController = new BatchManager.Builder()
                .setSerializationStrategy(serializationStrategy)
                .setBatchingStrategy(sizeBatchingStrategy)
                .setHandler(handler)
                .setOnBatchReadyListener(onBatchReadyListener)
                .build(context);

        batchController.addToBatch(Utils.fakeCollection(4));
        shadowLooper.runToEndOfTasks();
    }
}