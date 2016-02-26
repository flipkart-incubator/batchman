package com.flipkart.batching.listener;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.flipkart.Utils;
import com.flipkart.batching.BaseTestClass;
import com.flipkart.batching.Batch;
import com.flipkart.batching.BuildConfig;
import com.flipkart.batching.Data;
import com.flipkart.batching.exception.SerializeException;
import com.flipkart.batching.persistence.ByteArraySerializationStrategy;
import com.flipkart.batching.persistence.SerializationStrategy;
import com.flipkart.batching.strategy.SizeBatchingStrategy;
import com.squareup.tape.QueueFile;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
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
import static org.mockito.Mockito.when;

/**
 * Created by anirudh.r on 19/02/16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class PersistedBatchReadyTest extends BaseTestClass {

    /**
     * Test to verify that {@link PersistedBatchReadyListener#onInitialized(QueueFile)} is called
     */
    @Test
    public void testIfInitializedCalled() {
        File file = createRandomFile();
        HandlerThread handlerThread = new HandlerThread(createRandomString());
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        Handler handler = new Handler(looper);

        SizeBatchingStrategy strategy = mock(SizeBatchingStrategy.class);
        SizeBatchingStrategy.SizeBatch<Data> sizeBatchInfo = new SizeBatchingStrategy.SizeBatch<>(Utils.fakeCollection(5), 5);
        SerializationStrategy serializationStrategy = new ByteArraySerializationStrategy<>();
        PersistedBatchCallback persistedBatchCallback = mock(PersistedBatchCallback.class);
        PersistedBatchReadyListener persistedBatchReadyListener = new PersistedBatchReadyListener(file, serializationStrategy, handler, persistedBatchCallback);
        persistedBatchReadyListener.onReady(strategy, sizeBatchInfo);
        shadowLooper.runToEndOfTasks();
        //should be initialized first time
        Assert.assertTrue(persistedBatchReadyListener.isInitialized());
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
        SizeBatchingStrategy.SizeBatch<Data> sizeBatchInfo = new SizeBatchingStrategy.SizeBatch<>(Utils.fakeCollection(5), 5);
        SerializationStrategy serializationStrategy = new ByteArraySerializationStrategy<>();
        PersistedBatchCallback persistedBatchCallback = mock(PersistedBatchCallback.class);

        PersistedBatchReadyListener persistedBatchReadyListener = new PersistedBatchReadyListener(file, serializationStrategy, handler, persistedBatchCallback);
        persistedBatchReadyListener.onReady(strategy, sizeBatchInfo);
        shadowLooper.runToEndOfTasks();

        verify(persistedBatchCallback, times(1)).onPersistSuccess(any(Batch.class));
    }

    /**
     * Test if Finish method gets called
     *
     * @throws IOException
     * @throws SerializeException
     */
    @Test
    public void testFinishCalled() throws IOException, SerializeException {
        File file = createRandomFile();
        QueueFile queueFile = mock(QueueFile.class);
        HandlerThread handlerThread = new HandlerThread(createRandomString());
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        Handler handler = new Handler(looper);
        SizeBatchingStrategy strategy = mock(SizeBatchingStrategy.class);
        SizeBatchingStrategy.SizeBatch<Data> sizeBatchInfo = new SizeBatchingStrategy.SizeBatch<>(Utils.fakeCollection(5), 5);
        SerializationStrategy serializationStrategy = new ByteArraySerializationStrategy<>();
        PersistedBatchCallback persistedBatchCallback = mock(PersistedBatchCallback.class);
        PersistedBatchReadyListener persistedBatchReadyListener = new PersistedBatchReadyListener(file, serializationStrategy, handler, persistedBatchCallback);
        ArrayList<Data> arrayList = Utils.fakeCollection(4);

        when(queueFile.peek()).thenReturn(serializationStrategy.serializeCollection(arrayList));
        persistedBatchReadyListener.onReady(strategy, sizeBatchInfo);
        shadowLooper.runToEndOfTasks();

        persistedBatchReadyListener.finish(sizeBatchInfo);
        shadowLooper.runToEndOfTasks();

        Assert.assertNotNull(queueFile.peek());

        doThrow(new IOException()).when(queueFile).remove();
        persistedBatchReadyListener.finish(sizeBatchInfo);
    }

    /**
     * Test to verify {@link PersistedBatchCallback#onPersistFailure(Batch, Exception)}
     *
     * @throws SerializeException
     * @throws IOException
     */
    @Test
    public void testIfPersistFailureCalled() throws SerializeException, IOException {
        File file = createRandomFile();
        QueueFile queueFile = mock(QueueFile.class);
        HandlerThread handlerThread = new HandlerThread(createRandomString());
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        Handler handler = new Handler(looper);
        SizeBatchingStrategy strategy = mock(SizeBatchingStrategy.class);
        SizeBatchingStrategy.SizeBatch<Data> sizeBatchInfo = new SizeBatchingStrategy.SizeBatch<>(Utils.fakeCollection(5), 5);
        SerializationStrategy serializationStrategy = new ByteArraySerializationStrategy<>();
        PersistedBatchCallback persistedBatchCallback = mock(PersistedBatchCallback.class);
        PersistedBatchReadyListener persistedBatchReadyListener = new PersistedBatchReadyListener(file, serializationStrategy, handler, persistedBatchCallback);

        byte[] data = serializationStrategy.serializeCollection(Utils.fakeAdsCollection(4));
        doThrow(new IOException()).when(queueFile).add(data);
        persistedBatchReadyListener.onReady(strategy, sizeBatchInfo);
        shadowLooper.runToEndOfTasks();
    }

    @Test
    public void testListenerNotNull() {
        File file = createRandomFile();
        HandlerThread handlerThread = new HandlerThread(createRandomString());
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        Handler handler = new Handler(looper);
        SizeBatchingStrategy strategy = mock(SizeBatchingStrategy.class);
        SizeBatchingStrategy.SizeBatch<Data> sizeBatchInfo = new SizeBatchingStrategy.SizeBatch<>(Utils.fakeCollection(5), 5);
        SerializationStrategy serializationStrategy = new ByteArraySerializationStrategy<>();
        PersistedBatchCallback persistedBatchCallback = mock(PersistedBatchCallback.class);
        PersistedBatchReadyListener persistedBatchReadyListener = new PersistedBatchReadyListener(file, serializationStrategy, handler, persistedBatchCallback);
        persistedBatchReadyListener.onReady(strategy, sizeBatchInfo);

        Assert.assertTrue(persistedBatchReadyListener.getListener() != null);
    }

    @After
    public void afterTest() {
        deleteRandomFiles();
    }
}
