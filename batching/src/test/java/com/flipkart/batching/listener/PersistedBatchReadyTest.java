/*
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2017 Flipkart Internet Pvt. Ltd.
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

package com.flipkart.batching.listener;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.flipkart.Utils;
import com.flipkart.batching.BaseTestClass;
import com.flipkart.batching.BuildConfig;
import com.flipkart.batching.core.Batch;
import com.flipkart.batching.core.Data;
import com.flipkart.batching.core.SerializationStrategy;
import com.flipkart.batching.core.batch.SizeBatch;
import com.flipkart.batching.core.exception.SerializeException;
import com.flipkart.batching.gson.GsonSerializationStrategy;
import com.flipkart.batching.strategy.SizeBatchingStrategy;
import com.flipkart.batching.tape.QueueFile;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Test for {@link PersistedBatchReadyListener}
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class PersistedBatchReadyTest extends BaseTestClass {

    @Test
    public void testIfInitializedCalled() {
        File file = createRandomFile();
        HandlerThread handlerThread = new HandlerThread(createRandomString());
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        Handler handler = new Handler(looper);

        SizeBatchingStrategy strategy = mock(SizeBatchingStrategy.class);
        SizeBatch<Data> sizeBatchInfo = new SizeBatch<>(Utils.fakeCollection(5), 5);
        PersistedBatchCallback persistedBatchCallback = mock(PersistedBatchCallback.class);
        SerializationStrategy serializationStrategy = new GsonSerializationStrategy();
        serializationStrategy.build();

        PersistedBatchReadyListener<Data, Batch<Data>> persistedBatchReadyListener = new PersistedBatchReadyListener<>(createRandomFile().getPath(), serializationStrategy, handler, persistedBatchCallback);
        persistedBatchReadyListener.onReady(strategy, sizeBatchInfo);
        shadowLooper.runToEndOfTasks();
        //should be initialized first time
        Assert.assertTrue(persistedBatchReadyListener.isInitialized());
        Assert.assertTrue(persistedBatchReadyListener.getQueueFile() != null);
    }

    /**
     * Test to verify {@link PersistedBatchCallback#onPersistSuccess(Batch)}
     */
    @Test
    public void testIfPersistSuccessCalled() {
        File file = createRandomFile();
        HandlerThread handlerThread = new HandlerThread(createRandomString());
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        Handler handler = new Handler(looper);
        SizeBatchingStrategy strategy = mock(SizeBatchingStrategy.class);
        SizeBatch<Data> sizeBatchInfo = new SizeBatch<>(Utils.fakeCollection(5), 5);
        SerializationStrategy serializationStrategy = new GsonSerializationStrategy();
        serializationStrategy.build();
        PersistedBatchCallback persistedBatchCallback = mock(PersistedBatchCallback.class);

        PersistedBatchReadyListener persistedBatchReadyListener = new PersistedBatchReadyListener(createRandomFile().getPath(), serializationStrategy, handler, persistedBatchCallback);
        persistedBatchReadyListener.onReady(strategy, sizeBatchInfo);
        shadowLooper.runToEndOfTasks();

        //verify that it gets called once
        verify(persistedBatchCallback, times(1)).onPersistSuccess(any(Batch.class));
    }

    /**
     * Test if Finish method gets called
     *
     * @throws IOException
     * @throws SerializeException
     */
    @Test
    public void testFinishCalled() throws IOException {
        File file = createRandomFile();
        QueueFile queueFile = mock(QueueFile.class);
        HandlerThread handlerThread = new HandlerThread(createRandomString());
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        Handler handler = new Handler(looper);
        SizeBatchingStrategy strategy = mock(SizeBatchingStrategy.class);
        SizeBatch<Data> sizeBatchInfo = new SizeBatch<>(Utils.fakeCollection(5), 5);
        SerializationStrategy serializationStrategy = new GsonSerializationStrategy();
        serializationStrategy.build();

        PersistedBatchCallback persistedBatchCallback = mock(PersistedBatchCallback.class);
        PersistedBatchReadyListener persistedBatchReadyListener = new PersistedBatchReadyListener(createRandomFile().getPath(), serializationStrategy, handler, persistedBatchCallback);
        ArrayList<Data> arrayList = Utils.fakeCollection(4);

        when(queueFile.peek()).thenReturn(serializationStrategy.serializeCollection(arrayList));
        persistedBatchReadyListener.onReady(strategy, sizeBatchInfo);
        shadowLooper.runToEndOfTasks();

        persistedBatchReadyListener.finish(sizeBatchInfo);
        shadowLooper.runToEndOfTasks();

        Assert.assertNotNull(queueFile.peek());

        doThrow(new IOException()).when(queueFile).remove();
        persistedBatchReadyListener.finish(sizeBatchInfo);

        //verify that finish method gets called
        verify(persistedBatchCallback, times(1)).onFinish();
    }

    /**
     * Test that persist success is not called until finish is called.
     *
     * @throws IOException
     * @throws SerializeException
     */
    @Test
    public void testPersistSuccessNotCalledMoreThanOnce() throws IOException {
        File file = createRandomFile();
        HandlerThread handlerThread = new HandlerThread(createRandomString());
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        Handler handler = new Handler(looper);
        SizeBatchingStrategy strategy = mock(SizeBatchingStrategy.class);
        SerializationStrategy serializationStrategy = new GsonSerializationStrategy();
        serializationStrategy.build();
        PersistedBatchCallback persistedBatchCallback = mock(PersistedBatchCallback.class);
        PersistedBatchReadyListener persistedBatchReadyListener = new PersistedBatchReadyListener(createRandomFile().getPath(), serializationStrategy, handler, persistedBatchCallback);

        SizeBatch<Data> firstBatch = new SizeBatch<>(Utils.fakeCollection(5), 5);
        persistedBatchReadyListener.onReady(strategy, firstBatch);
        SizeBatch<Data> secondBatch = new SizeBatch<>(Utils.fakeCollection(5), 5);
        persistedBatchReadyListener.onReady(strategy, secondBatch);
        SizeBatch<Data> thirdBatch = new SizeBatch<>(Utils.fakeCollection(5), 5);
        persistedBatchReadyListener.onReady(strategy, thirdBatch);
        SizeBatch<Data> fourthBatch = new SizeBatch<>(Utils.fakeCollection(5), 5);
        persistedBatchReadyListener.onReady(strategy, fourthBatch);

        shadowLooper.runToEndOfTasks();

        //verify that it gets called with firstBatch
        verify(persistedBatchCallback, times(1)).onPersistSuccess(firstBatch);
        persistedBatchReadyListener.finish(firstBatch);
        shadowLooper.runToEndOfTasks();
        //verify that it gets called with secondBatch
        verify(persistedBatchCallback, times(1)).onPersistSuccess(secondBatch);
        persistedBatchReadyListener.finish(secondBatch);
        shadowLooper.runToEndOfTasks();
        //verify that it gets called with thirdBatch
        verify(persistedBatchCallback, times(1)).onPersistSuccess(thirdBatch);
        //verify that there is no more interactions
        verifyNoMoreInteractions(persistedBatchCallback);
    }

    /**
     * Test to verify that finish throws an {@link IllegalStateException} when it gets called with a different batch.
     * Ignoring this test because we are catching this exception and logging it now.
     * <p>
     * UPDATE : not throwing this exception anymore, thus ignore.
     *
     * @throws SerializeException
     * @throws IOException
     */
    @Ignore
    @Test(expected = IllegalStateException.class)
    public void testFinishException() throws IOException {
        File file = createRandomFile();
        QueueFile queueFile = mock(QueueFile.class);
        HandlerThread handlerThread = new HandlerThread(createRandomString());
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        Handler handler = new Handler(looper);
        SizeBatchingStrategy strategy = mock(SizeBatchingStrategy.class);
        SizeBatch<Data> sizeBatchInfo = new SizeBatch<>(Utils.fakeCollection(5), 5);
        SerializationStrategy serializationStrategy = new GsonSerializationStrategy();
        serializationStrategy.build();

        PersistedBatchCallback persistedBatchCallback = mock(PersistedBatchCallback.class);
        PersistedBatchReadyListener persistedBatchReadyListener = new PersistedBatchReadyListener(createRandomFile().getPath(), serializationStrategy, handler, persistedBatchCallback);
        byte[] peeked = serializationStrategy.serializeBatch(sizeBatchInfo);

        when(queueFile.peek()).thenReturn(peeked);
        persistedBatchReadyListener.onReady(strategy, sizeBatchInfo);
        shadowLooper.runToEndOfTasks();

        SizeBatch<Data> sizeBatchInfo1 = new SizeBatch<>(Utils.fakeAdsCollection(4), 5);
        try {
            persistedBatchReadyListener.finish(sizeBatchInfo1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        shadowLooper.runToEndOfTasks();
    }

    /**
     * Test to verify {@link PersistedBatchCallback#onPersistFailure(Batch, Exception)}
     *
     * @throws SerializeException
     * @throws IOException
     */
    @Test
    public void testIfPersistFailureCalled() throws IOException {
        File file = createRandomFile();
        QueueFile queueFile = mock(QueueFile.class);
        HandlerThread handlerThread = new HandlerThread(createRandomString());
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        Handler handler = new Handler(looper);
        SizeBatchingStrategy strategy = mock(SizeBatchingStrategy.class);
        SizeBatch<Data> sizeBatchInfo = new SizeBatch<>(Utils.fakeCollection(5), 5);
        SerializationStrategy serializationStrategy = new GsonSerializationStrategy();
        serializationStrategy.build();
        PersistedBatchCallback persistedBatchCallback = mock(PersistedBatchCallback.class);
        PersistedBatchReadyListener persistedBatchReadyListener = new PersistedBatchReadyListener(createRandomFile().getPath(), serializationStrategy, handler, persistedBatchCallback);

        byte[] data = serializationStrategy.serializeCollection(Utils.fakeAdsCollection(4));
        doThrow(new IOException()).when(queueFile).add(data);
        persistedBatchReadyListener.onReady(strategy, sizeBatchInfo);
        shadowLooper.runToEndOfTasks();
    }

    /**
     * Test to verify that {@link PersistedBatchReadyListener#getListener()} is not null
     */
    @Test
    public void testListenerNotNull() {
        File file = createRandomFile();
        HandlerThread handlerThread = new HandlerThread(createRandomString());
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        Handler handler = new Handler(looper);
        SizeBatchingStrategy strategy = mock(SizeBatchingStrategy.class);
        SizeBatch<Data> sizeBatchInfo = new SizeBatch<>(Utils.fakeCollection(5), 5);
        SerializationStrategy serializationStrategy = new GsonSerializationStrategy();
        serializationStrategy.build();
        PersistedBatchCallback persistedBatchCallback = mock(PersistedBatchCallback.class);
        PersistedBatchReadyListener persistedBatchReadyListener = new PersistedBatchReadyListener(createRandomFile().getPath(), serializationStrategy, handler, persistedBatchCallback);
        persistedBatchReadyListener.onReady(strategy, sizeBatchInfo);

        Assert.assertTrue(persistedBatchReadyListener.getListener() != null);

        persistedBatchReadyListener = new PersistedBatchReadyListener(createRandomFile().getPath(), serializationStrategy, handler, null);
        persistedBatchReadyListener.setListener(persistedBatchCallback);
        Assert.assertTrue(persistedBatchReadyListener.getListener() != null);
    }


    /**
     * Delete all the test_files after test ends
     */
    @After
    public void afterTest() {
        deleteRandomFiles();
    }
}
