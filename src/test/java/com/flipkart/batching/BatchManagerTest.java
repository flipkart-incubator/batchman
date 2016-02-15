package com.flipkart.batching;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.flipkart.Utils;
import com.flipkart.data.Data;
import com.flipkart.persistence.PersistenceStrategy;
import com.flipkart.persistence.SerializationStrategy;

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
 * Created by anirudh.r on 12/02/16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class BatchManagerTest {

    @Mock
    SerializationStrategy serializationStrategy;
    @Mock
    PersistenceStrategy persistenceStrategy;
    @Mock
    OnBatchReadyListener onBatchReadyListener;
    @Mock
    Context context;
    @Mock
    Data eventData;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test to verify {@link OnBatchReadyListener#onReady(Collection)} is called when batch is ready
     */
    @Test
    public void testAddToBatch() {
        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        Handler handler = new Handler(looper);
        SizeBatchingStrategy sizeBatchingStrategy = new SizeBatchingStrategy(5, persistenceStrategy);
        BatchController batchController = new BatchManager.Builder()
                .setSerializationStrategy(serializationStrategy)
                .setBatchingStrategy(sizeBatchingStrategy)
                .setHandler(handler)
                .setOnBatchReadyListener(onBatchReadyListener)
                .build(context);
        ArrayList<Data> fakeCollection = Utils.fakeCollection(5);

        when(persistenceStrategy.getData()).thenReturn(fakeCollection);
        batchController.addToBatch(fakeCollection);
        shadowLooper.runToEndOfTasks();
        verify(onBatchReadyListener, times(1)).onReady(eq(fakeCollection));
    }

//    @Test(expected = IllegalAccessError.class)
//    public void testIfBatchStrategyNotInitialized() {
//        BatchingStrategy batchingStrategy = mock(SizeBatchingStrategy.class);
//        HandlerThread handlerThread = new HandlerThread("test");
//        handlerThread.start();
//        Looper looper = handlerThread.getLooper();
//        shadowLooper = Shadows.shadowOf(looper);
//        Handler handler = new Handler(looper);
//        BatchController batchController = new BatchManager.Builder()
//                .setSerializationStrategy(serializationStrategy)
//                .setBatchingStrategy(batchingStrategy)
//                .setHandler(handler)
//                .setOnBatchReadyListener(onBatchReadyListener)
//                .build(context);
//        when(batchingStrategy.isInitialized()).thenReturn(false);
//        ArrayList<Data> fakeCollection = Utils.fakeCollection(5);
//        batchController.addToBatch(fakeCollection);
//    }

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
}
