package com.flipkart.batching;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.webkit.ValueCallback;

import com.flipkart.Utils;
import com.flipkart.batching.listener.NetworkPersistedBatchReadyListener;
import com.flipkart.batching.listener.NetworkPersistedBatchReadyListener.NetworkBatchListener;
import com.flipkart.batching.listener.TrimPersistedBatchReadyListener;
import com.flipkart.batching.persistence.GsonSerializationStrategy;
import com.flipkart.batching.persistence.PersistenceStrategy;
import com.flipkart.batching.persistence.SerializationStrategy;
import com.flipkart.batching.persistence.TapePersistenceStrategy;
import com.flipkart.batching.strategy.BaseBatchingStrategy;
import com.flipkart.batching.strategy.ComboBatchingStrategy;
import com.flipkart.batching.strategy.SizeBatchingStrategy;
import com.flipkart.batching.strategy.TimeBatchingStrategy;
import com.flipkart.batching.tape.QueueFile;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by anirudh.r on 12/02/16.
 * Test for {@link BatchManager}
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class BatchManagerTest extends BaseTestClass {

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

    @Test
    public void testReInitialized() {
        ShadowLooper shadowLooper = Shadows.shadowOf(Looper.getMainLooper());
        Handler handler = new Handler();

        String filePath = createRandomString();
        String filePath1 = createRandomString();
        SerializationStrategy<Data, Batch<Data>> serializationStrategy = new GsonSerializationStrategy<>();
        TapePersistenceStrategy<Data> persistenceStrategy = new TapePersistenceStrategy<>(filePath1, serializationStrategy);
        Context context = RuntimeEnvironment.application;
        BatchingStrategy sizeBatchingStrategy = new SizeBatchingStrategy(2, persistenceStrategy);
        TimeBatchingStrategy timeBatchingStrategy = new TimeBatchingStrategy(5000, persistenceStrategy);
        ComboBatchingStrategy comboBatchingStrategy = new ComboBatchingStrategy(timeBatchingStrategy, sizeBatchingStrategy);

        NetworkBatchListener batchListener = new NetworkBatchListener() {
            @Override
            public void performNetworkRequest(Batch batch, ValueCallback callback) {
                callback.onReceiveValue(new NetworkPersistedBatchReadyListener.NetworkRequestResponse(false, 500));
            }
        };
        NetworkPersistedBatchReadyListener batchReadyListener = new NetworkPersistedBatchReadyListener(context, filePath, serializationStrategy, handler, batchListener, 2, 50, 50, TrimPersistedBatchReadyListener.MODE_TRIM_AT_START, null);
        BatchController batchController = new BatchManager.Builder()
                .setSerializationStrategy(serializationStrategy)
                .setBatchingStrategy(comboBatchingStrategy)
                .setHandler(handler)
                .setOnBatchReadyListener(batchReadyListener)
                .build(context);

        ArrayList<Data> firstBatch = Utils.fakeCollection(2);
        ArrayList<Data> secondBatch = Utils.fakeCollection(1);
        ArrayList<Data> thirdBatch = Utils.fakeCollection(1);
        ArrayList<Data> fourthBatch = Utils.fakeCollection(1);
        ArrayList<Data> fifthBatch = Utils.fakeCollection(1);

        batchController.addToBatch(firstBatch);
        shadowLooper.runToEndOfTasks();
        batchController.addToBatch(secondBatch);
        shadowLooper.runToEndOfTasks();
        batchController.addToBatch(thirdBatch);
        shadowLooper.runToEndOfTasks();

        QueueFile oldQueueFile = batchReadyListener.getQueueFile();
        Assert.assertTrue(oldQueueFile.size() == 3);
        batchReadyListener.setQueueFile(oldQueueFile);

        final ArrayList outputData = new ArrayList();

        NetworkBatchListener batchListener2 = new NetworkBatchListener() {
            @Override
            public void performNetworkRequest(Batch batch, ValueCallback callback) {
                callback.onReceiveValue(new NetworkPersistedBatchReadyListener.NetworkRequestResponse(true, 200));
                outputData.addAll(batch.getDataCollection());
            }

            @Override
            public boolean isNetworkConnected(Context context) {
                return true;
            }
        };

        persistenceStrategy = new TapePersistenceStrategy<>(filePath1, serializationStrategy);
        sizeBatchingStrategy = new SizeBatchingStrategy(2, persistenceStrategy);
        timeBatchingStrategy = new TimeBatchingStrategy(5000, persistenceStrategy);
        comboBatchingStrategy = new ComboBatchingStrategy(timeBatchingStrategy, sizeBatchingStrategy);

        NetworkBatchListener batchListener2Spy = spy(batchListener2);
        batchReadyListener = new NetworkPersistedBatchReadyListener(context, filePath, serializationStrategy, handler, batchListener2Spy, 2, 50, 50, TrimPersistedBatchReadyListener.MODE_TRIM_AT_START, null);

        batchController = new BatchManager.Builder()
                .setSerializationStrategy(serializationStrategy)
                .setBatchingStrategy(comboBatchingStrategy)
                .setHandler(handler)
                .setOnBatchReadyListener(batchReadyListener)
                .build(context);

        batchController.addToBatch(fourthBatch);
        batchController.addToBatch(fifthBatch);
        shadowLooper.runToEndOfTasks();

        verify(batchListener2Spy, times(4)).performNetworkRequest(any(Batch.class), any(ValueCallback.class));
        Assert.assertTrue(outputData.size() == 6);

    }
}