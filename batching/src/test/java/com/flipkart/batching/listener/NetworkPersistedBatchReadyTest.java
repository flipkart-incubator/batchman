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

package com.flipkart.batching.listener;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.webkit.ValueCallback;

import com.flipkart.Utils;
import com.flipkart.batching.BaseTestClass;
import com.flipkart.batching.BatchManager;
import com.flipkart.batching.BuildConfig;
import com.flipkart.batching.gson.GsonSerializationStrategy;
import com.flipkart.batching.core.Batch;
import com.flipkart.batching.core.Data;
import com.flipkart.batching.core.SerializationStrategy;
import com.flipkart.batching.core.batch.SizeBatch;
import com.flipkart.batching.core.exception.SerializeException;
import com.flipkart.batching.strategy.SizeBatchingStrategy;
import com.flipkart.batching.tape.ObjectQueue;

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
import java.util.ArrayList;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Test for {@link NetworkPersistedBatchReadyListener}
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class NetworkPersistedBatchReadyTest extends BaseTestClass {

    /**
     * Test to verify the retry policy for 5XX server errors
     *
     * @throws IOException
     * @throws SerializeException
     */
    @Test
    public void test5XXRetryPolicy() throws IOException {
        int ERROR_CODE_5XX = 500;
        long callbackIdle = 1000;
        int maxRetryCount = 5;

        Context context = RuntimeEnvironment.application;
        File file = createRandomFile();
        HandlerThread handlerThread = new HandlerThread(createRandomString());
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper);
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        SizeBatchingStrategy strategy = mock(SizeBatchingStrategy.class);
        SizeBatch<Data> firstBatch = new SizeBatch<>(Utils.fakeCollection(5), 5);
        SerializationStrategy serializationStrategy = new GsonSerializationStrategy();
        serializationStrategy.build();

        TrimmedBatchCallback trimmedBatchCallback = mock(TrimmedBatchCallback.class);
        MockNetworkPersistedBatchReadyListener networkBatchListener = spy(new MockNetworkPersistedBatchReadyListener(new NetworkPersistedBatchReadyListener.NetworkRequestResponse(true, ERROR_CODE_5XX), handler, callbackIdle, context));
        NetworkPersistedBatchReadyListener networkPersistedBatchReadyListener = new NetworkPersistedBatchReadyListener(context, createRandomFile().getPath(), serializationStrategy, handler, networkBatchListener, maxRetryCount, 50, 10, TrimPersistedBatchReadyListener.MODE_TRIM_AT_START, trimmedBatchCallback);
        networkPersistedBatchReadyListener.onReady(strategy, firstBatch);
        shadowLooper.runToEndOfTasks();

        //verify that it gets called once
        verify(networkBatchListener, times(1)).performNetworkRequest(any(Batch.class), any(ValueCallback.class));
        //verify that it gets called 2 times
        shadowLooper.idle(networkPersistedBatchReadyListener.getDefaultTimeoutMs());
        verify(networkBatchListener, times(2)).performNetworkRequest(any(Batch.class), any(ValueCallback.class));
        //verify that it gets called 3 times
        shadowLooper.idle(callbackIdle + networkPersistedBatchReadyListener.getDefaultTimeoutMs() * 2);
        verify(networkBatchListener, times(3)).performNetworkRequest(any(Batch.class), any(ValueCallback.class));
        //verify that it gets called 4 times
        shadowLooper.idle(callbackIdle + networkPersistedBatchReadyListener.getDefaultTimeoutMs() * 4);
        verify(networkBatchListener, times(4)).performNetworkRequest(any(Batch.class), any(ValueCallback.class));
        //verify that it gets called 5 times
        shadowLooper.idle(callbackIdle + networkPersistedBatchReadyListener.getDefaultTimeoutMs() * 8);
        verify(networkBatchListener, times(5)).performNetworkRequest(any(Batch.class), any(ValueCallback.class));
        //verify that it does not gets called after 5 times
        shadowLooper.idle(callbackIdle + networkPersistedBatchReadyListener.getDefaultTimeoutMs() * 16);

        ArgumentCaptor<ValueCallback> valueCallbackCapture = ArgumentCaptor.forClass(ValueCallback.class);
        verify(networkBatchListener, times(5)).performNetworkRequest(any(Batch.class), valueCallbackCapture.capture());

        shadowLooper.runToEndOfTasks();
        //verify that it does not gets called again
        verify(networkBatchListener, times(5)).performNetworkRequest(any(Batch.class), any(ValueCallback.class));

        NetworkPersistedBatchReadyListener.NetworkRequestResponse requestResponse = new NetworkPersistedBatchReadyListener.NetworkRequestResponse(true, 200);
        valueCallbackCapture.getValue().onReceiveValue(requestResponse);

        shadowLooper.runToEndOfTasks();
        verify(networkBatchListener, times(5)).performNetworkRequest(any(Batch.class), any(ValueCallback.class));

        networkPersistedBatchReadyListener.onReady(strategy, firstBatch);
        shadowLooper.runToEndOfTasks();
        //verify that it gets called once
        verify(networkBatchListener, times(6)).performNetworkRequest(any(Batch.class), any(ValueCallback.class));
        //verify that it gets called 2 times
        shadowLooper.idle(networkPersistedBatchReadyListener.getDefaultTimeoutMs());
        verify(networkBatchListener, times(7)).performNetworkRequest(any(Batch.class), any(ValueCallback.class));
    }

    /**
     * Test to verify the retry policy for 4XX errors.
     *
     * @throws IOException
     * @throws SerializeException
     */
    @Test
    public void test4XXRetryPolicy() throws IOException {
        int ERROR_CODE_4XX = 400;
        long callbackIdle = 1000;
        int maxRetryCount = 5;

        Context context = RuntimeEnvironment.application;
        File file = createRandomFile();
        HandlerThread handlerThread = new HandlerThread(createRandomString());
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper);
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        SizeBatchingStrategy strategy = mock(SizeBatchingStrategy.class);
        SizeBatch<Data> firstBatch = new SizeBatch<>(Utils.fakeCollection(5), 5);
        SerializationStrategy serializationStrategy = new GsonSerializationStrategy();
        serializationStrategy.build();
        TrimmedBatchCallback trimmedBatchCallback = mock(TrimmedBatchCallback.class);

        MockNetworkPersistedBatchReadyListener networkBatchListener = spy(new MockNetworkPersistedBatchReadyListener(new NetworkPersistedBatchReadyListener.NetworkRequestResponse(true, ERROR_CODE_4XX), handler, callbackIdle, context));
        NetworkPersistedBatchReadyListener networkPersistedBatchReadyListener = new NetworkPersistedBatchReadyListener(context, createRandomFile().getPath(), serializationStrategy, handler, networkBatchListener, maxRetryCount, 50, 10, TrimPersistedBatchReadyListener.MODE_TRIM_AT_START, trimmedBatchCallback);
        networkPersistedBatchReadyListener.onReady(strategy, firstBatch);
        shadowLooper.runToEndOfTasks();
        //verify that it gets called once
        verify(networkBatchListener, times(1)).performNetworkRequest(any(Batch.class), any(ValueCallback.class));
        //verify that it does not gets called again
        shadowLooper.idle(networkPersistedBatchReadyListener.getDefaultTimeoutMs());
        verify(networkBatchListener, times(1)).performNetworkRequest(any(Batch.class), any(ValueCallback.class));
    }

    /**
     * Test to verify the retry policy for 2XX response codes.
     *
     * @throws IOException
     * @throws SerializeException
     */
    @Test
    public void test2XXRetryPolicy() throws IOException {
        int ERROR_CODE_2XX = 200;
        long callbackIdle = 1000;

        Context context = RuntimeEnvironment.application;
        File file = createRandomFile();
        HandlerThread handlerThread = new HandlerThread(createRandomString());
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper);
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        SizeBatchingStrategy strategy = mock(SizeBatchingStrategy.class);
        SizeBatch<Data> firstBatch = new SizeBatch<>(Utils.fakeCollection(5), 5);
        SerializationStrategy serializationStrategy = new GsonSerializationStrategy();
        serializationStrategy.build();
        TrimmedBatchCallback trimmedBatchCallback = mock(TrimmedBatchCallback.class);

        MockNetworkPersistedBatchReadyListener networkBatchListener = spy(new MockNetworkPersistedBatchReadyListener(new NetworkPersistedBatchReadyListener.NetworkRequestResponse(true, ERROR_CODE_2XX), handler, callbackIdle, context));
        NetworkPersistedBatchReadyListener networkPersistedBatchReadyListener = new NetworkPersistedBatchReadyListener(context, createRandomFile().getPath(), serializationStrategy, handler, networkBatchListener, 5, 50, 10, TrimPersistedBatchReadyListener.MODE_TRIM_AT_START, trimmedBatchCallback);
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
    public void testNetworkBroadcast() throws IOException {
        int ERROR_CODE_2XX = 200;
        long callbackIdle = 1000;

        Context context = RuntimeEnvironment.application;
        File file = createRandomFile();
        HandlerThread handlerThread = new HandlerThread(createRandomString());
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper);
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        SizeBatchingStrategy strategy = mock(SizeBatchingStrategy.class);
        SizeBatch<Data> firstBatch = new SizeBatch<>(Utils.fakeCollection(5), 3);
        SerializationStrategy serializationStrategy = new GsonSerializationStrategy();
        serializationStrategy.build();
        TrimmedBatchCallback trimmedBatchCallback = mock(TrimmedBatchCallback.class);

        MockNetworkPersistedBatchReadyListener networkBatchListener = spy(new MockNetworkPersistedBatchReadyListener(new NetworkPersistedBatchReadyListener.NetworkRequestResponse(true, ERROR_CODE_2XX), handler, callbackIdle, context));
        NetworkPersistedBatchReadyListener networkPersistedBatchReadyListener = new NetworkPersistedBatchReadyListener(context, createRandomFile().getPath(), serializationStrategy, handler, networkBatchListener, 5, 50, 10, TrimPersistedBatchReadyListener.MODE_TRIM_AT_START, trimmedBatchCallback);
        networkPersistedBatchReadyListener.onReady(strategy, firstBatch);
        shadowLooper.runToEndOfTasks();

        // PURPOSE : if network broadcast is received, perform request should only be called if no requests are pending
        ArgumentCaptor<ValueCallback> valueCallbackCapture = ArgumentCaptor.forClass(ValueCallback.class);
        verify(networkBatchListener, times(1)).performNetworkRequest(eq(firstBatch), valueCallbackCapture.capture());
        sendFakeNetworkBroadcast(context);
        shadowLooper.runToEndOfTasks();
        //verify that it does not gets called again
        verify(networkBatchListener, times(1)).performNetworkRequest(eq(firstBatch), any(ValueCallback.class));

        //PURPOSE : if network broadcast is received, perform any pending requests which were paused due to no network
        networkBatchListener.setMockedNetworkConnected(false); //simulating network not connected
        SizeBatch<Data> secondBatch = new SizeBatch<>(Utils.fakeCollection(5), 4);
        networkPersistedBatchReadyListener.onReady(strategy, secondBatch);
        shadowLooper.idle(100);
        // no new request is sent since no network exists
        verify(networkBatchListener, times(1)).performNetworkRequest(any(Batch.class), any(ValueCallback.class));
        networkBatchListener.setMockedNetworkConnected(true);
        // once network broadcast comes in we resume flow
        sendFakeNetworkBroadcast(context);
        shadowLooper.idle(100);
        ArgumentCaptor<Batch> batchCapture = ArgumentCaptor.forClass(Batch.class);
        // now we should get perform request callback with the new batch. Gets called
        verify(networkBatchListener, times(2)).performNetworkRequest(batchCapture.capture(), any(ValueCallback.class));
        //assert that value received in params is equal to the sent batch.
        Assert.assertEquals(batchCapture.getValue(), secondBatch);

        verify(networkBatchListener, atLeastOnce()).isNetworkConnected(context);
        verify(networkBatchListener, atLeastOnce()).setMockedNetworkConnected(anyBoolean());
        shadowLooper.runToEndOfTasks();
        //verify that there are no more interactions.
        verifyNoMoreInteractions(networkBatchListener);
    }

    @Test
    public void testReinitialize() {
        long callbackIdle = 1000;

        Context context = RuntimeEnvironment.application;
        File file = createRandomFile();
        Handler handler = new Handler();
        ShadowLooper shadowLooper = Shadows.shadowOf(Looper.getMainLooper());
        SizeBatchingStrategy strategy = mock(SizeBatchingStrategy.class);
        SerializationStrategy serializationStrategy = new GsonSerializationStrategy();
        serializationStrategy.build();
        TrimmedBatchCallback trimmedBatchCallback = mock(TrimmedBatchCallback.class);

        NetworkPersistedBatchReadyListener.NetworkRequestResponse networkRequestResponse = new NetworkPersistedBatchReadyListener.NetworkRequestResponse(false, 500);
        MockNetworkPersistedBatchReadyListener networkBatchListener = spy(new MockNetworkPersistedBatchReadyListener(networkRequestResponse, handler, callbackIdle, context));
        NetworkPersistedBatchReadyListener networkPersistedBatchReadyListener = new NetworkPersistedBatchReadyListener(context, createRandomFile().getPath(), serializationStrategy, handler, networkBatchListener, 1, 50, 10, TrimPersistedBatchReadyListener.MODE_TRIM_AT_START, trimmedBatchCallback);
        SizeBatch<Data> firstBatch = new SizeBatch<>(Utils.fakeCollection(5), 3);
        networkPersistedBatchReadyListener.onReady(strategy, firstBatch);
        shadowLooper.runToEndOfTasks();
        SizeBatch<Data> secondBatch = new SizeBatch<>(Utils.fakeCollection(5), 3);
        networkPersistedBatchReadyListener.onReady(strategy, secondBatch);
        shadowLooper.runToEndOfTasks();

        ObjectQueue<Batch<Data>> oldQueueFile = networkPersistedBatchReadyListener.getQueueFile();

        //reinitialize
        networkPersistedBatchReadyListener = new NetworkPersistedBatchReadyListener(context, createRandomFile().getPath(), serializationStrategy, handler, networkBatchListener, 5, 50, 10, TrimPersistedBatchReadyListener.MODE_TRIM_AT_START, trimmedBatchCallback);
        networkPersistedBatchReadyListener.setQueueFile(oldQueueFile); //since 2 queuefiles cannot point to same disk file, we had to do this
        networkRequestResponse.complete = true;
        networkRequestResponse.httpErrorCode = 200;
        SizeBatch<Data> thirdBatch = new SizeBatch<>(Utils.fakeCollection(5), 3);
        networkPersistedBatchReadyListener.onReady(strategy, thirdBatch);
        shadowLooper.runToEndOfTasks(); //all retries finished


    }

    /**
     * Test to verify the retry policy
     */
    @Test
    public void testRetryPolicy() {
        int errorCode = 500;
        long callbackIdle = 1000;
        int retryCount = 4;

        Context context = RuntimeEnvironment.application;
        File file = createRandomFile();
        ShadowLooper shadowLooper = Shadows.shadowOf(Looper.getMainLooper());
        Handler handler = new Handler();
        SizeBatchingStrategy strategy = mock(SizeBatchingStrategy.class);
        SizeBatch<Data> firstBatch = new SizeBatch<>(Utils.fakeCollection(5), 5);
        SerializationStrategy serializationStrategy = new GsonSerializationStrategy();
        serializationStrategy.build();
        NetworkPersistedBatchReadyListener.NetworkRequestResponse requestResponse = new NetworkPersistedBatchReadyListener.NetworkRequestResponse(true, errorCode);
        TrimmedBatchCallback trimmedBatchCallback = mock(TrimmedBatchCallback.class);

        MockNetworkPersistedBatchReadyListener networkBatchListener = spy(new MockNetworkPersistedBatchReadyListener(requestResponse, handler, callbackIdle, context));
        NetworkPersistedBatchReadyListener networkPersistedBatchReadyListener = new NetworkPersistedBatchReadyListener(context, createRandomFile().getPath(), serializationStrategy, handler, networkBatchListener, retryCount, 50, 10, TrimPersistedBatchReadyListener.MODE_TRIM_AT_START, trimmedBatchCallback);
        networkPersistedBatchReadyListener.onReady(strategy, firstBatch);
        shadowLooper.runToEndOfTasks();

        //verify that it gets called 1 times
        verify(networkBatchListener, times(1)).performNetworkRequest(eq(firstBatch), any(ValueCallback.class));
        shadowLooper.idle(networkPersistedBatchReadyListener.getDefaultTimeoutMs());
        //verify that it gets called 2 times after waiting for specified time
        verify(networkBatchListener, times(2)).performNetworkRequest(eq(firstBatch), any(ValueCallback.class));
        shadowLooper.idle(callbackIdle + networkPersistedBatchReadyListener.getDefaultTimeoutMs() * 2);
        //verify that it gets called 3 times after waiting for specified time
        verify(networkBatchListener, times(3)).performNetworkRequest(eq(firstBatch), any(ValueCallback.class));
        //verify that it gets called 3 times after waiting for specified time
        shadowLooper.idle(callbackIdle + networkPersistedBatchReadyListener.getDefaultTimeoutMs() * 4);
        //verify that it gets called 4 times after waiting for specified time
        verify(networkBatchListener, times(4)).performNetworkRequest(eq(firstBatch), any(ValueCallback.class));
        sendFakeNetworkBroadcast(context);
        shadowLooper.idle(callbackIdle + networkPersistedBatchReadyListener.getDefaultTimeoutMs() * 8);
        // now it should have stopped retrying anymore since max retry is reached
        verify(networkBatchListener, times(4)).performNetworkRequest(eq(firstBatch), any(ValueCallback.class));

        sendFakeNetworkBroadcast(context);
        SizeBatch<Data> secondBatch = new SizeBatch<>(Utils.fakeCollection(5), 5);
        networkPersistedBatchReadyListener.onReady(strategy, secondBatch);

        shadowLooper.idle();
        // note : now flow will get resumed, which mean network request for first batch is retried (NOT second batch)
        verify(networkBatchListener, times(5)).performNetworkRequest(eq(firstBatch), any(ValueCallback.class));
        shadowLooper.idle(callbackIdle + networkPersistedBatchReadyListener.getDefaultTimeoutMs());
        sendFakeNetworkBroadcast(context);
        verify(networkBatchListener, times(6)).performNetworkRequest(eq(firstBatch), any(ValueCallback.class));

        requestResponse.complete = true;
        requestResponse.httpErrorCode = 200;
        sendFakeNetworkBroadcast(context);
        shadowLooper.idle(callbackIdle + networkPersistedBatchReadyListener.getDefaultTimeoutMs() * 2); // this will call second batch
        //verify that it gets called 1 time with new batch
        verify(networkBatchListener, times(1)).performNetworkRequest(eq(secondBatch), any(ValueCallback.class));
        shadowLooper.runToEndOfTasks();

        verify(networkBatchListener, atLeastOnce()).isNetworkConnected(context);
        verifyNoMoreInteractions(networkBatchListener);
    }

    @Test
    public void testFinishCalledIfRemoveAfterMaxRetryTrue() {
        int errorCode = 500;
        long callbackIdle = 1000;
        int retryCount = 4;

        Context context = RuntimeEnvironment.application;
        File file = createRandomFile();
        ShadowLooper shadowLooper = Shadows.shadowOf(Looper.getMainLooper());
        Handler handler = new Handler();
        SizeBatchingStrategy strategy = mock(SizeBatchingStrategy.class);
        SizeBatch<Data> firstBatch = new SizeBatch<>(Utils.fakeCollection(5), 5);
        SerializationStrategy serializationStrategy = new GsonSerializationStrategy();
        serializationStrategy.build();
        NetworkPersistedBatchReadyListener.NetworkRequestResponse requestResponse = new NetworkPersistedBatchReadyListener.NetworkRequestResponse(true, errorCode);
        TrimmedBatchCallback trimmedBatchCallback = mock(TrimmedBatchCallback.class);

        MockNetworkPersistedBatchReadyListener networkBatchListener = spy(new MockNetworkPersistedBatchReadyListener(requestResponse, handler, callbackIdle, context));
        NetworkPersistedBatchReadyListener networkPersistedBatchReadyListener = new NetworkPersistedBatchReadyListener(context, createRandomFile().getPath(), serializationStrategy, handler, networkBatchListener, retryCount, 50, 10, TrimPersistedBatchReadyListener.MODE_TRIM_AT_START, trimmedBatchCallback);
        networkPersistedBatchReadyListener.onReady(strategy, firstBatch);
        networkPersistedBatchReadyListener.setCallFinishAfterMaxRetry(true);
        shadowLooper.runToEndOfTasks();

        //verify that it gets called 1 times
        verify(networkBatchListener, times(1)).performNetworkRequest(eq(firstBatch), any(ValueCallback.class));
        shadowLooper.idle(networkPersistedBatchReadyListener.getDefaultTimeoutMs());
        //verify that it gets called 2 times after waiting for specified time
        verify(networkBatchListener, times(2)).performNetworkRequest(eq(firstBatch), any(ValueCallback.class));
        shadowLooper.idle(callbackIdle + networkPersistedBatchReadyListener.getDefaultTimeoutMs() * 2);
        //verify that it gets called 3 times after waiting for specified time
        verify(networkBatchListener, times(3)).performNetworkRequest(eq(firstBatch), any(ValueCallback.class));
        //verify that it gets called 3 times after waiting for specified time
        shadowLooper.idle(callbackIdle + networkPersistedBatchReadyListener.getDefaultTimeoutMs() * 4);
        //verify that it gets called 4 times after waiting for specified time
        verify(networkBatchListener, times(4)).performNetworkRequest(eq(firstBatch), any(ValueCallback.class));
        shadowLooper.idle(callbackIdle + networkPersistedBatchReadyListener.getDefaultTimeoutMs() * 8);
        // now it should have stopped retrying anymore since max retry is reached
        verify(networkBatchListener, times(4)).performNetworkRequest(eq(firstBatch), any(ValueCallback.class));

        assertTrue(networkPersistedBatchReadyListener.callFinishWithBatch(firstBatch));
    }


    /**
     * Test to verify {@link NetworkPersistedBatchReadyListener#getDefaultTimeoutMs()} and {@link NetworkPersistedBatchReadyListener#getDefaultBackoffMultiplier()}
     * setter properties
     */
    @Test
    public void testRetryTimeOut() {
        int errorCode = 500;
        long callbackIdle = 1000;
        int retryCount = 4;
        int timeOut = 2500;
        float backOffMultiplier = 1f;
        int maxQueueSize = 5;
        int trimToSize = 2;

        Context context = RuntimeEnvironment.application;
        File file = createRandomFile();
        ShadowLooper shadowLooper = Shadows.shadowOf(Looper.getMainLooper());
        Handler handler = new Handler();
        SizeBatchingStrategy strategy = mock(SizeBatchingStrategy.class);
        SizeBatch<Data> firstBatch = new SizeBatch<>(Utils.fakeCollection(5), 5);
        SerializationStrategy serializationStrategy = new GsonSerializationStrategy();
        serializationStrategy.build();
        NetworkPersistedBatchReadyListener.NetworkRequestResponse requestResponse = new NetworkPersistedBatchReadyListener.NetworkRequestResponse(true, errorCode);
        TrimmedBatchCallback trimmedBatchCallback = mock(TrimmedBatchCallback.class);

        MockNetworkPersistedBatchReadyListener networkBatchListener = spy(new MockNetworkPersistedBatchReadyListener(requestResponse, handler, callbackIdle, context));
        NetworkPersistedBatchReadyListener networkPersistedBatchReadyListener = new NetworkPersistedBatchReadyListener(context, createRandomFile().getPath(), serializationStrategy, handler, networkBatchListener, retryCount, 50, 10, TrimPersistedBatchReadyListener.MODE_TRIM_AT_START, trimmedBatchCallback);
        networkPersistedBatchReadyListener.setDefaultTimeoutMs(timeOut);
        networkPersistedBatchReadyListener.setDefaultBackoffMultiplier(backOffMultiplier);

        Assert.assertEquals(networkPersistedBatchReadyListener.getDefaultTimeoutMs(), timeOut);
        Assert.assertEquals(networkPersistedBatchReadyListener.getDefaultBackoffMultiplier(), backOffMultiplier);
    }

    /**
     * Test to verify {@link NetworkPersistedBatchReadyListener.NetworkBatchListener#isConnectedToNetwork()}
     */
    @Test
    public void testConnectedToNetwork() {
        Context context = RuntimeEnvironment.application;
        NetworkPersistedBatchReadyListener.NetworkBatchListener networkBatchListener = new NetworkPersistedBatchReadyListener.NetworkBatchListener() {
            @Override
            public void performNetworkRequest(Batch batch, ValueCallback callback) {

            }

            @Override
            public boolean isNetworkConnected(Context context) {
                return super.isNetworkConnected(context);
            }
        };

        assertTrue(networkBatchListener.isNetworkConnected(context));
    }

    /**
     * Method to send fake BroadcastReceiver for network change
     *
     * @param context
     */
    private void sendFakeNetworkBroadcast(Context context) {
        Intent intent = new Intent();
        intent.setAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.sendBroadcast(intent);
    }

    /**
     * Delete all the test_files when the test ends
     */
//    @After
//    public void afterTest() {
//        deleteRandomFiles();
//    }
    @After
    public void tearDown() throws Exception {
        deleteRandomFiles();
    }

    @Test
    public void testDoNotTrimmingIfWaitingForFinish() throws Exception {
        int ERROR_CODE_2XX = 200;
        long callbackIdle = 5000;
        Context context = RuntimeEnvironment.application;

        GsonSerializationStrategy<Data, Batch<Data>> serializationStrategy = new GsonSerializationStrategy<>();
        serializationStrategy.build();

        HandlerThread handlerThread = new HandlerThread(createRandomString());
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        Handler handler = new Handler(looper);

        TrimmedBatchCallback trimmedBatchCallback = mock(TrimmedBatchCallback.class);
        SizeBatchingStrategy sizeBatchingStrategy = mock(SizeBatchingStrategy.class);
        final ArrayList<Data> dataList1 = Utils.fakeAdsCollection(10);
        SizeBatch<Data> sizeBatch1 = new SizeBatch<>(dataList1, 3);

        MockNetworkPersistedBatchReadyListener networkBatchListener = spy(new MockNetworkPersistedBatchReadyListener(new NetworkPersistedBatchReadyListener.NetworkRequestResponse(true, ERROR_CODE_2XX), handler, callbackIdle, context));
        NetworkPersistedBatchReadyListener networkPersistedBatchReadyListener = new NetworkPersistedBatchReadyListener(context, createRandomFile().getPath(), serializationStrategy, handler, networkBatchListener, 1, 2, 1, TrimPersistedBatchReadyListener.MODE_TRIM_AT_START | TrimPersistedBatchReadyListener.MODE_TRIM_ON_READY, trimmedBatchCallback);
        networkPersistedBatchReadyListener.onReady(sizeBatchingStrategy, sizeBatch1);
        shadowLooper.idle(100); //this will start waiting for finish callback
        final ArrayList<Data> dataList2 = Utils.fakeAdsCollection(10);
        SizeBatch<Data> sizeBatch2 = new SizeBatch<>(dataList2, 3);
        networkPersistedBatchReadyListener.onReady(sizeBatchingStrategy,sizeBatch2);
        shadowLooper.idle(100); // this will initiate a trim since size is 2
        final ArrayList<Data> dataList3 = Utils.fakeAdsCollection(10);
        SizeBatch<Data> sizeBatch3 = new SizeBatch<>(dataList2, 3);
        networkPersistedBatchReadyListener.onReady(sizeBatchingStrategy,sizeBatch2);
        shadowLooper.idle(100); // this will fire onReady once more

        shadowLooper.runToEndOfTasks(); //this will invoke the network callback because the idle time is high


    }

    /**
     * Custom MockNetworkPersistedBatchReadyListener
     */
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