package com.flipkart.batching.listener;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.webkit.ValueCallback;

import com.flipkart.Utils;
import com.flipkart.batching.BaseTestClass;
import com.flipkart.batching.Batch;
import com.flipkart.batching.BuildConfig;
import com.flipkart.batching.Data;
import com.flipkart.batching.exception.SerializeException;
import com.flipkart.batching.persistence.ByteArraySerializationStrategy;
import com.flipkart.batching.persistence.SerializationStrategy;
import com.flipkart.batching.strategy.SizeBatchingStrategy;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import java.io.File;
import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by anirudh.r on 19/02/16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class NetworkPersistedBatchReadyTest extends BaseTestClass {

    /**
     * Test to verify the retry policy for 5XX errors
     *
     * @throws IOException
     * @throws SerializeException
     */
    @Test
    public void test5XXRetryPolicy() throws IOException, SerializeException {
        Context context = RuntimeEnvironment.application;
        File file = createRandomFile();
        HandlerThread handlerThread = new HandlerThread(createRandomString());
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper);
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        SizeBatchingStrategy strategy = mock(SizeBatchingStrategy.class);
        SizeBatchingStrategy.SizeBatch<Data> firstBatch = new SizeBatchingStrategy.SizeBatch<>(Utils.fakeCollection(5), 5);
        SerializationStrategy serializationStrategy = new ByteArraySerializationStrategy();
        int ERROR_CODE_5XX = 500;
        MockNetworkPersistedBatchReadyListener networkBatchListener = spy(new MockNetworkPersistedBatchReadyListener(new NetworkPersistedBatchReadyListener.NetworkRequestResponse(true, ERROR_CODE_5XX)));
        int maxRetryCount = 5;
        NetworkPersistedBatchReadyListener networkPersistedBatchReadyListener = new NetworkPersistedBatchReadyListener(context, file, serializationStrategy, handler, networkBatchListener, maxRetryCount);
        networkPersistedBatchReadyListener.onReady(strategy, firstBatch);
        shadowLooper.runToEndOfTasks();
        //verify that it gets called once
        verify(networkBatchListener, times(1)).performNetworkRequest(any(Batch.class), any(ValueCallback.class));
        //verify that it gets called 2 times after waiting for 5000ms
        shadowLooper.idle(5000);
        verify(networkBatchListener, times(2)).performNetworkRequest(any(Batch.class), any(ValueCallback.class));
        //verify that it gets called 3 times after waiting for 10000ms(5000 + 5000)
        shadowLooper.idle(10000);
        verify(networkBatchListener, times(3)).performNetworkRequest(any(Batch.class), any(ValueCallback.class));
        //verify that it gets called 4 times after waiting for 20000ms(10000 + 10000)
        shadowLooper.idle(20000);
        verify(networkBatchListener, times(4)).performNetworkRequest(any(Batch.class), any(ValueCallback.class));
        //verify that it gets called 5 times after waiting for 40000ms(20000 + 20000)
        shadowLooper.idle(40000);
        verify(networkBatchListener, times(5)).performNetworkRequest(any(Batch.class), any(ValueCallback.class));
        //verify that it does not gets called after 5 times
        shadowLooper.idle(80000);
        verify(networkBatchListener, times(5)).performNetworkRequest(any(Batch.class), any(ValueCallback.class));
    }

    /**
     * Test to verify the retry policy for 4XX errors.
     * Call {@link NetworkPersistedBatchReadyListener#makeNetworkRequest(Batch)} only once, IF server returns 4XX error, discard the batch.
     *
     * @throws IOException
     * @throws SerializeException
     */
    @Test
    public void test4XXRetryPolicy() throws IOException, SerializeException {
        Context context = RuntimeEnvironment.application;
        File file = createRandomFile();
        HandlerThread handlerThread = new HandlerThread(createRandomString());
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper);
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        SizeBatchingStrategy strategy = mock(SizeBatchingStrategy.class);
        SizeBatchingStrategy.SizeBatch<Data> firstBatch = new SizeBatchingStrategy.SizeBatch<>(Utils.fakeCollection(5), 5);
        SerializationStrategy serializationStrategy = new ByteArraySerializationStrategy();
        int ERROR_CODE_4XX = 400;
        MockNetworkPersistedBatchReadyListener networkBatchListener = spy(new MockNetworkPersistedBatchReadyListener(new NetworkPersistedBatchReadyListener.NetworkRequestResponse(true, ERROR_CODE_4XX)));
        int maxRetryCount = 5;
        NetworkPersistedBatchReadyListener networkPersistedBatchReadyListener = new NetworkPersistedBatchReadyListener(context, file, serializationStrategy, handler, networkBatchListener, maxRetryCount);
        networkPersistedBatchReadyListener.onReady(strategy, firstBatch);
        shadowLooper.runToEndOfTasks();
        //verify that it gets called once
        verify(networkBatchListener, times(1)).performNetworkRequest(any(Batch.class), any(ValueCallback.class));
        //verify that it does not gets called
        shadowLooper.idle(5000);
        verify(networkBatchListener, times(1)).performNetworkRequest(any(Batch.class), any(ValueCallback.class));
    }

    /**
     * Test to verify the retry policy for 2XX response codes.
     *
     * @throws IOException
     * @throws SerializeException
     */
    @Test
    public void test2XXRetryPolicy() throws IOException, SerializeException {
        Context context = RuntimeEnvironment.application;
        File file = createRandomFile();
        HandlerThread handlerThread = new HandlerThread(createRandomString());
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper);
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        SizeBatchingStrategy strategy = mock(SizeBatchingStrategy.class);
        SizeBatchingStrategy.SizeBatch<Data> firstBatch = new SizeBatchingStrategy.SizeBatch<>(Utils.fakeCollection(5), 5);
        SerializationStrategy serializationStrategy = new ByteArraySerializationStrategy();
        int ERROR_CODE_2XX = 200;
        MockNetworkPersistedBatchReadyListener networkBatchListener = spy(new MockNetworkPersistedBatchReadyListener(new NetworkPersistedBatchReadyListener.NetworkRequestResponse(true, ERROR_CODE_2XX)));
        NetworkPersistedBatchReadyListener networkPersistedBatchReadyListener = new NetworkPersistedBatchReadyListener(context, file, serializationStrategy, handler, networkBatchListener, 0);
        networkPersistedBatchReadyListener.onReady(strategy, firstBatch);
        shadowLooper.runToEndOfTasks();
        //verify that it gets called once
        verify(networkBatchListener, times(1)).performNetworkRequest(any(Batch.class), any(ValueCallback.class));
    }

    @After
    public void afterTest() {
        deleteRandomFiles();
    }

    private static class MockNetworkPersistedBatchReadyListener implements NetworkPersistedBatchReadyListener.NetworkBatchListener {
        NetworkPersistedBatchReadyListener.NetworkRequestResponse requestResponse;

        public MockNetworkPersistedBatchReadyListener(NetworkPersistedBatchReadyListener.NetworkRequestResponse requestResponse) {
            this.requestResponse = requestResponse;
        }

        @Override
        public void performNetworkRequest(final Batch batch, final ValueCallback callback) {
            callback.onReceiveValue(requestResponse);
        }
    }
}