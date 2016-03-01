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

import junit.framework.Assert;

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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;

/**
 * Created by anirudh.r on 19/02/16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class NetworkPersistedBatchReadyTest extends BaseTestClass {

    /**
     * Test that persist success is not called until finish is called.
     *
     * @throws IOException
     * @throws SerializeException
     */
    @Test
    public void testPersistSuccessNotCalledMoreThanOnce() throws IOException, SerializeException {
        Context context = RuntimeEnvironment.application;
        File file = createRandomFile();
        HandlerThread handlerThread = new HandlerThread(createRandomString());
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper);
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);

        final SizeBatchingStrategy strategy = mock(SizeBatchingStrategy.class);
        final SizeBatchingStrategy.SizeBatch<Data> firstBatch = new SizeBatchingStrategy.SizeBatch<>(Utils.fakeCollection(5), 5);
        SerializationStrategy serializationStrategy = new ByteArraySerializationStrategy();
        MockNetworkPersistedBatchReadyListener networkBatchListener = new MockNetworkPersistedBatchReadyListener(new NetworkPersistedBatchReadyListener.NetworkRequestResponse(true, 500), 5, shadowLooper);

        int maxRetryCount = 5;
        final NetworkPersistedBatchReadyListener networkPersistedBatchReadyListener = new NetworkPersistedBatchReadyListener(context, file, serializationStrategy, handler, networkBatchListener, maxRetryCount);
        handler.post(new Runnable() {
            @Override
            public void run() {
                networkPersistedBatchReadyListener.onReady(strategy, firstBatch);
            }
        });

        shadowLooper.runToEndOfTasks();
        Assert.assertTrue(networkBatchListener.await(100000));
    }

    @After
    public void afterTest() {
        deleteRandomFiles();
    }

    private static class MockNetworkPersistedBatchReadyListener implements NetworkPersistedBatchReadyListener.NetworkBatchListener {
        int count;
        int currentCount;
        CountDownLatch countDownLatch;
        ShadowLooper shadowLooper;
        NetworkPersistedBatchReadyListener.NetworkRequestResponse requestResponse;

        public MockNetworkPersistedBatchReadyListener(NetworkPersistedBatchReadyListener.NetworkRequestResponse requestResponse, int count, ShadowLooper shadowLooper) {
            this.requestResponse = requestResponse;
            countDownLatch = new CountDownLatch(1);
            this.count = count;
            currentCount = 0;
            this.shadowLooper = shadowLooper;
        }

        @Override
        public void performNetworkRequest(final Batch batch, final ValueCallback callback) {
            callback.onReceiveValue(requestResponse);
            countDownLatch.countDown();
        }

        public boolean await(int timeout) {
            try {
                if (!countDownLatch.await(timeout, TimeUnit.MILLISECONDS)) {
                    return false;
                } else {
                    currentCount++;
                    if (currentCount == count) {
                        return true;
                    } else {
                        this.shadowLooper.runToEndOfTasks();
                        return await(timeout);
                    }
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}
