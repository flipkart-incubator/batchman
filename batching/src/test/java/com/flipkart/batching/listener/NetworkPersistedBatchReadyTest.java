package com.flipkart.batching.listener;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.webkit.ValueCallback;

import com.flipkart.Utils;
import com.flipkart.batching.BaseTestClass;
import com.flipkart.batching.Batch;
import com.flipkart.batching.BatchManager;
import com.flipkart.batching.BuildConfig;
import com.flipkart.batching.Data;
import com.flipkart.batching.exception.SerializeException;
import com.flipkart.batching.persistence.GsonSerializationStrategy;
import com.flipkart.batching.persistence.SerializationStrategy;
import com.flipkart.batching.strategy.SizeBatchingStrategy;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import java.io.File;
import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

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
        SerializationStrategy serializationStrategy = new GsonSerializationStrategy();
        BatchManager.registerBuiltInTypes(serializationStrategy);
        serializationStrategy.build();
        int ERROR_CODE_5XX = 500;
        long callbackIdle = 1000;
        MockNetworkPersistedBatchReadyListener networkBatchListener = spy(new MockNetworkPersistedBatchReadyListener(new NetworkPersistedBatchReadyListener.NetworkRequestResponse(true, ERROR_CODE_5XX), handler, callbackIdle, context));
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
        shadowLooper.idle(callbackIdle + 10000);
        verify(networkBatchListener, times(3)).performNetworkRequest(any(Batch.class), any(ValueCallback.class));
        //verify that it gets called 4 times after waiting for 20000ms(10000 + 10000)
        shadowLooper.idle(callbackIdle + 20000);
        verify(networkBatchListener, times(4)).performNetworkRequest(any(Batch.class), any(ValueCallback.class));
        //verify that it gets called 5 times after waiting for 40000ms(20000 + 20000)
        shadowLooper.idle(callbackIdle + 40000);
        verify(networkBatchListener, times(5)).performNetworkRequest(any(Batch.class), any(ValueCallback.class));
        //verify that it does not gets called after 5 times
        shadowLooper.idle(callbackIdle + 80000);
        ArgumentCaptor<ValueCallback> valueCallbackCapture = ArgumentCaptor.forClass(ValueCallback.class);
        verify(networkBatchListener, times(5)).performNetworkRequest(any(Batch.class), valueCallbackCapture.capture());

        shadowLooper.runToEndOfTasks();
        verify(networkBatchListener, times(5)).performNetworkRequest(any(Batch.class), any(ValueCallback.class));

        NetworkPersistedBatchReadyListener.NetworkRequestResponse requestResponse = new NetworkPersistedBatchReadyListener.NetworkRequestResponse(true, 200);
        valueCallbackCapture.getValue().onReceiveValue(requestResponse);

        shadowLooper.runToEndOfTasks();
        verify(networkBatchListener, times(5)).performNetworkRequest(any(Batch.class), any(ValueCallback.class));

        networkPersistedBatchReadyListener.onReady(strategy, firstBatch);
        shadowLooper.runToEndOfTasks();
        //verify that it gets called once
        verify(networkBatchListener, times(6)).performNetworkRequest(any(Batch.class), any(ValueCallback.class));
        //verify that it gets called 2 times after waiting for 5000ms
        shadowLooper.idle(5000);
        verify(networkBatchListener, times(7)).performNetworkRequest(any(Batch.class), any(ValueCallback.class));



    }

    /**
     * Test to verify the retry policy for 4XX errors.
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
        SerializationStrategy serializationStrategy = new GsonSerializationStrategy();
        BatchManager.registerBuiltInTypes(serializationStrategy);
        serializationStrategy.build();
        int ERROR_CODE_4XX = 400;
        long callbackIdle = 1000;
        MockNetworkPersistedBatchReadyListener networkBatchListener = spy(new MockNetworkPersistedBatchReadyListener(new NetworkPersistedBatchReadyListener.NetworkRequestResponse(true, ERROR_CODE_4XX), handler, callbackIdle, context));
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
        SerializationStrategy serializationStrategy = new GsonSerializationStrategy();
        BatchManager.registerBuiltInTypes(serializationStrategy);
        serializationStrategy.build();

        BatchManager.registerBuiltInTypes(serializationStrategy);
        serializationStrategy.build();
        int ERROR_CODE_2XX = 200;
        long callbackIdle = 1000;
        MockNetworkPersistedBatchReadyListener networkBatchListener = spy(new MockNetworkPersistedBatchReadyListener(new NetworkPersistedBatchReadyListener.NetworkRequestResponse(true, ERROR_CODE_2XX), handler, callbackIdle, context));
        NetworkPersistedBatchReadyListener networkPersistedBatchReadyListener = new NetworkPersistedBatchReadyListener(context, file, serializationStrategy, handler, networkBatchListener, 0);
        networkPersistedBatchReadyListener.onReady(strategy, firstBatch);
        shadowLooper.runToEndOfTasks();
        //verify that it gets called once
        verify(networkBatchListener, times(1)).performNetworkRequest(any(Batch.class), any(ValueCallback.class));
    }

    /**
     * Test to verify the retry policy for 2XX response codes.
     *
     * @throws IOException
     * @throws SerializeException
     */
    @Test
    public void testNetworkBroadcast() throws IOException, SerializeException {
        Context context = RuntimeEnvironment.application;
        File file = createRandomFile();
        HandlerThread handlerThread = new HandlerThread(createRandomString());
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper);
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        SizeBatchingStrategy strategy = mock(SizeBatchingStrategy.class);
        SizeBatchingStrategy.SizeBatch<Data> firstBatch = new SizeBatchingStrategy.SizeBatch<>(Utils.fakeCollection(5), 5);
        SerializationStrategy serializationStrategy = new GsonSerializationStrategy();
        BatchManager.registerBuiltInTypes(serializationStrategy);
        serializationStrategy.build();
        int ERROR_CODE_2XX = 200;
        long callbackIdle = 1000;
        MockNetworkPersistedBatchReadyListener networkBatchListener = spy(new MockNetworkPersistedBatchReadyListener(new NetworkPersistedBatchReadyListener.NetworkRequestResponse(true, ERROR_CODE_2XX), handler, callbackIdle, context));
        NetworkPersistedBatchReadyListener networkPersistedBatchReadyListener = new NetworkPersistedBatchReadyListener(context, file, serializationStrategy, handler, networkBatchListener, 0);
        networkPersistedBatchReadyListener.onReady(strategy, firstBatch);
        shadowLooper.runToEndOfTasks();


        // PURPOSE : if network broadcast is received, perform request should only be called if no requests are pending
        ArgumentCaptor<ValueCallback> valueCallbackCapture = ArgumentCaptor.forClass(ValueCallback.class);
        verify(networkBatchListener, times(1)).performNetworkRequest(any(Batch.class), valueCallbackCapture.capture());
        sendFakeNetworkBroadcast(context);
        shadowLooper.idle(100);
        verify(networkBatchListener, times(1)).performNetworkRequest(any(Batch.class), any(ValueCallback.class));
        NetworkPersistedBatchReadyListener.NetworkRequestResponse requestResponse = new NetworkPersistedBatchReadyListener.NetworkRequestResponse(true, 200);
        valueCallbackCapture.getValue().onReceiveValue(requestResponse);


        //PURPOSE : if network broadcast is received, perform any pending requests which were paused due to no network
        networkBatchListener.setMockedNetworkConnected(false); //simulating network not connected
        SizeBatchingStrategy.SizeBatch<Data> secondBatch = new SizeBatchingStrategy.SizeBatch<>(Utils.fakeCollection(5), 5);
        networkPersistedBatchReadyListener.onReady(strategy, secondBatch);
        shadowLooper.idle(100);
        // no new request is sent since no network exists
        verify(networkBatchListener, times(1)).performNetworkRequest(any(Batch.class), any(ValueCallback.class));
        networkBatchListener.setMockedNetworkConnected(true);
        // once network broadcast comes in we resume flow
        sendFakeNetworkBroadcast(context);
        shadowLooper.idle(100);
        ArgumentCaptor<Batch> batchCapture = ArgumentCaptor.forClass(Batch.class);
        // now we should get perform request callback with the new batch.
        verify(networkBatchListener, times(2)).performNetworkRequest(batchCapture.capture(), any(ValueCallback.class));
        Assert.assertEquals(batchCapture.getValue(),secondBatch);

        verify(networkBatchListener, atLeastOnce()).isNetworkConnected(context);
        verify(networkBatchListener, atLeastOnce()).setMockedNetworkConnected(anyBoolean());
        shadowLooper.runToEndOfTasks();
        verifyNoMoreInteractions(networkBatchListener);
    }

    @Test
    public void testRetryPolicy() {
        Context context = RuntimeEnvironment.application;
        File file = createRandomFile();
        HandlerThread handlerThread = new HandlerThread(createRandomString());
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper);
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        SizeBatchingStrategy strategy = mock(SizeBatchingStrategy.class);
        SizeBatchingStrategy.SizeBatch<Data> firstBatch = new SizeBatchingStrategy.SizeBatch<>(Utils.fakeCollection(5), 5);
        SerializationStrategy serializationStrategy = new GsonSerializationStrategy();
        BatchManager.registerBuiltInTypes(serializationStrategy);
        serializationStrategy.build();
        int ERROR_CODE_2XX = 200;
        long callbackIdle = 1000;
        MockNetworkPersistedBatchReadyListener networkBatchListener = spy(new MockNetworkPersistedBatchReadyListener(new NetworkPersistedBatchReadyListener.NetworkRequestResponse(true, ERROR_CODE_2XX), handler, callbackIdle, context));
        NetworkPersistedBatchReadyListener networkPersistedBatchReadyListener = new NetworkPersistedBatchReadyListener(context, file, serializationStrategy, handler, networkBatchListener, 0);
        networkPersistedBatchReadyListener.onReady(strategy, firstBatch);
        shadowLooper.runToEndOfTasks();


    }

    private void sendFakeNetworkBroadcast(Context context) {
        Intent intent = new Intent();
        intent.setAction(Context.CONNECTIVITY_SERVICE);
        context.sendBroadcast(intent);
    }

    @After
    public void afterTest() {
        deleteRandomFiles();
    }

    private static class MockNetworkPersistedBatchReadyListener extends NetworkPersistedBatchReadyListener.NetworkBatchListener {
        private final Handler handler;
        private final long callbackIdle;
        private final Context context;
        NetworkPersistedBatchReadyListener.NetworkRequestResponse requestResponse;
        private boolean mockedNetworkConnected = true;

        public MockNetworkPersistedBatchReadyListener(NetworkPersistedBatchReadyListener.NetworkRequestResponse requestResponse, Handler handler, long callbackIdle, Context context) {
            this.requestResponse = requestResponse;
            this.callbackIdle = callbackIdle;
            this.handler = handler;
            this.context = context;
        }

        public void setMockedNetworkConnected(boolean mockedNetworkConnected) {
            this.mockedNetworkConnected = mockedNetworkConnected;
        }

        @Override
        public void performNetworkRequest(final Batch batch, final ValueCallback callback) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    callback.onReceiveValue(requestResponse);
                }
            }, callbackIdle);
        }

        @Override
        public boolean isNetworkConnected(Context context) {
            return mockedNetworkConnected;
        }
    }
}