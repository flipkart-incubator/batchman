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
import com.flipkart.batching.persistence.PersistenceStrategy;
import com.flipkart.batching.persistence.TapePersistenceStrategy;
import com.flipkart.batching.strategy.BaseBatchingStrategy;
import com.flipkart.batching.strategy.SizeBatchingStrategy;
import com.flipkart.batching.strategy.SizeTimeBatchingStrategy;
import com.flipkart.batching.tape.ObjectQueue;
import com.flipkart.batching.gson.GsonSerializationStrategy;
import com.flipkart.batching.core.Batch;
import com.flipkart.batching.core.BatchImpl;
import com.flipkart.batching.core.Data;
import com.flipkart.batching.core.SerializationStrategy;

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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for {@link BatchManager}
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
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
        when(persistenceStrategy.getDataSize()).thenReturn(fakeCollection.size());
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
                .setOnBatchReadyListener(onBatchReadyListener).registerBatchInfoType(BatchImpl.class)
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

        String filePath = createRandomFile().getPath();
        String filePath1 = createRandomFile().getPath();
        SerializationStrategy<Data, Batch<Data>> serializationStrategy = new GsonSerializationStrategy<>();
        TapePersistenceStrategy<Data> persistenceStrategy = new TapePersistenceStrategy<>(filePath1, serializationStrategy);
        Context context = RuntimeEnvironment.application;
        SizeTimeBatchingStrategy sizeTimeBatchingStrategy = new SizeTimeBatchingStrategy(persistenceStrategy, 2, 5000);

        NetworkBatchListener batchListener = new NetworkBatchListener() {
            @Override
            public void performNetworkRequest(Batch batch, ValueCallback callback) {
                callback.onReceiveValue(new NetworkPersistedBatchReadyListener.NetworkRequestResponse(false, 500));
            }
        };
        NetworkPersistedBatchReadyListener batchReadyListener = new NetworkPersistedBatchReadyListener(context, filePath, serializationStrategy, handler, batchListener, 2, 50, 50, TrimPersistedBatchReadyListener.MODE_TRIM_AT_START, null);
        BatchController batchController = new BatchManager.Builder()
                .setSerializationStrategy(serializationStrategy)
                .setBatchingStrategy(sizeTimeBatchingStrategy)
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

        ObjectQueue<Batch<Data>> oldQueueFile = batchReadyListener.getQueueFile();
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
        sizeTimeBatchingStrategy = new SizeTimeBatchingStrategy(persistenceStrategy, 2, 5000);

        NetworkBatchListener batchListener2Spy = spy(batchListener2);
        batchReadyListener = new NetworkPersistedBatchReadyListener(context, filePath, serializationStrategy, handler, batchListener2Spy, 2, 50, 50, TrimPersistedBatchReadyListener.MODE_TRIM_AT_START, null);

        batchController = new BatchManager.Builder()
                .setSerializationStrategy(serializationStrategy)
                .setBatchingStrategy(sizeTimeBatchingStrategy)
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