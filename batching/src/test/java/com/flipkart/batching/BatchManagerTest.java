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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by anirudh.r on 12/02/16.
 * Test for {@link BatchManager}
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class BatchManagerTest {

    /**
     * Test for {@link BatchManager#addToBatch(Collection)}
     */
    @Test
    public void testAddToBatch() {
        SerializationStrategy<Data, Batch<Data>> serializationStrategy = mock(SerializationStrategy.class);
        PersistenceStrategy<Data> persistenceStrategy = mock(PersistenceStrategy.class);
        OnBatchReadyListener<Data, Batch<Data>> onBatchReadyListener = mock(OnBatchReadyListener.class);
        Context context = RuntimeEnvironment.application;

        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
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
        //verify that it gets called once
        verify(onBatchReadyListener, times(1)).onReady(eq(sizeBatchingStrategy), any(Batch.class));
    }

    /**
     * Test to verify that handler is not null
     */
    @Test
    public void testHandlerNotNull() {
        SerializationStrategy<Data, Batch<Data>> serializationStrategy = mock(SerializationStrategy.class);
        PersistenceStrategy<Data> persistenceStrategy = mock(PersistenceStrategy.class);
        OnBatchReadyListener<Data, Batch<Data>> onBatchReadyListener = mock(OnBatchReadyListener.class);
        Context context = RuntimeEnvironment.application;

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
        //assert that handler is not null
        Assert.assertNotNull(batchController.getHandler());

        batchController = new BatchManager.Builder()
                .setSerializationStrategy(serializationStrategy)
                .setBatchingStrategy(sizeBatchingStrategy)
                .setHandler(null)
                .setOnBatchReadyListener(onBatchReadyListener)
                .build(context);

        Assert.assertNotNull(batchController.getHandler());
    }

    /**
     * Test to verify that BatchingStrategy is not null
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBatchingStrategyNullException() {
        SerializationStrategy<Data, Batch<Data>> serializationStrategy = mock(SerializationStrategy.class);
        OnBatchReadyListener<Data, Batch<Data>> onBatchReadyListener = mock(OnBatchReadyListener.class);
        Context context = RuntimeEnvironment.application;

        //will throw an exception
        new BatchManager.Builder()
                .setSerializationStrategy(serializationStrategy)
                .setBatchingStrategy(null)
                .setHandler(null)
                .setOnBatchReadyListener(onBatchReadyListener)
                .build(context);
    }

    /**
     * Test to verify that SerializationStrategy is not null
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSerializationStrategyNullException() {
        PersistenceStrategy<Data> persistenceStrategy = mock(PersistenceStrategy.class);
        OnBatchReadyListener<Data, Batch<Data>> onBatchReadyListener = mock(OnBatchReadyListener.class);
        Context context = RuntimeEnvironment.application;
        SizeBatchingStrategy sizeBatchingStrategy = new SizeBatchingStrategy(5, persistenceStrategy);

        //will throw an exception
        new BatchManager.Builder()
                .setSerializationStrategy(null)
                .setBatchingStrategy(sizeBatchingStrategy)
                .setHandler(null)
                .setOnBatchReadyListener(onBatchReadyListener)
                .build(context);
    }

    /**
     * Test to verify that OnBatchReadyListener is not null
     */
    @Test(expected = IllegalArgumentException.class)
    public void testOnReadyListenerNullException() {
        PersistenceStrategy<Data> persistenceStrategy = mock(PersistenceStrategy.class);
        Context context = RuntimeEnvironment.application;
        SizeBatchingStrategy sizeBatchingStrategy = new SizeBatchingStrategy(5, persistenceStrategy);
        SerializationStrategy<Data, Batch<Data>> serializationStrategy = mock(SerializationStrategy.class);

        BatchController batchController = new BatchManager.Builder()
                .setSerializationStrategy(serializationStrategy)
                .setBatchingStrategy(sizeBatchingStrategy)
                .setHandler(null)
                .setOnBatchReadyListener(null)
                .build(context);
    }

    /**
     * Test for {@link BatchManager#registerSuppliedTypes(BatchManager.Builder, SerializationStrategy)}
     */
    @Test
    public void testRegisterSuppliedTypes() {
        PersistenceStrategy<Data> persistenceStrategy = mock(PersistenceStrategy.class);
        Context context = RuntimeEnvironment.application;
        SizeBatchingStrategy sizeBatchingStrategy = new SizeBatchingStrategy(5, persistenceStrategy);
        OnBatchReadyListener<Data, Batch<Data>> onBatchReadyListener = mock(OnBatchReadyListener.class);
        SerializationStrategy<Data, Batch<Data>> serializationStrategy = mock(SerializationStrategy.class);

        BatchController batchController = new BatchManager.Builder()
                .setSerializationStrategy(serializationStrategy)
                .setBatchingStrategy(sizeBatchingStrategy)
                .setHandler(null)
                .setOnBatchReadyListener(onBatchReadyListener).registerDataType(Data.class)
                .build(context);

        verify(serializationStrategy, times(1)).registerDataType(Data.class);
    }

    /**
     * Test for {@link BatchManager#registerBuiltInTypes(SerializationStrategy)}
     */
    @Test
    public void testRegisterBatchInfoType() {
        PersistenceStrategy<Data> persistenceStrategy = mock(PersistenceStrategy.class);
        Context context = RuntimeEnvironment.application;
        SizeBatchingStrategy sizeBatchingStrategy = new SizeBatchingStrategy(5, persistenceStrategy);
        OnBatchReadyListener<Data, Batch<Data>> onBatchReadyListener = mock(OnBatchReadyListener.class);
        SerializationStrategy<Data, Batch<Data>> serializationStrategy = mock(SerializationStrategy.class);

        new BatchManager.Builder<>()
                .setSerializationStrategy(serializationStrategy)
                .setBatchingStrategy(sizeBatchingStrategy)
                .setHandler(null)
                .setOnBatchReadyListener(onBatchReadyListener).registerBatchInfoType(Batch.class)
                .build(context);

    }

    /**
     * Test for {@link BatchManager#getSerializationStrategy()}
     */
    @Test
    public void testGetSerializationStrategy() {
        PersistenceStrategy<Data> persistenceStrategy = mock(PersistenceStrategy.class);
        Context context = RuntimeEnvironment.application;
        SizeBatchingStrategy sizeBatchingStrategy = new SizeBatchingStrategy(5, persistenceStrategy);
        OnBatchReadyListener<Data, Batch<Data>> onBatchReadyListener = mock(OnBatchReadyListener.class);
        SerializationStrategy<Data, Batch<Data>> serializationStrategy = mock(SerializationStrategy.class);

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
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        Handler handler = new Handler(looper);
        PersistenceStrategy<Data> persistenceStrategy = mock(PersistenceStrategy.class);
        Context context = RuntimeEnvironment.application;
        OnBatchReadyListener<Data, Batch<Data>> onBatchReadyListener = mock(OnBatchReadyListener.class);
        SerializationStrategy<Data, Batch<Data>> serializationStrategy = mock(SerializationStrategy.class);
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