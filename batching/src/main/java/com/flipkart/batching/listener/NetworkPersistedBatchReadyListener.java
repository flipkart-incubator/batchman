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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.webkit.ValueCallback;

import com.flipkart.batching.BatchingStrategy;
import com.flipkart.batchingcore.Batch;
import com.flipkart.batchingcore.Data;
import com.flipkart.batchingcore.SerializationStrategy;

import org.slf4j.LoggerFactory;

/**
 * Network Persisted Batch Ready Listener
 */
public class NetworkPersistedBatchReadyListener<E extends Data, T extends Batch<E>> extends TrimPersistedBatchReadyListener<E, T> {
    static final org.slf4j.Logger log = LoggerFactory.getLogger(NetworkPersistedBatchReadyListener.class);
    private static final int HTTP_SERVER_ERROR_CODE_RANGE_START = 500;
    private static final int HTTP_SERVER_ERROR_CODE_RANGE_END = 599;
    public int defaultTimeoutMs = 2500;
    public float defaultBackoffMultiplier = 1f;
    T lastBatch;
    int retryCount = 0;
    int mCurrentTimeoutMs = 0;
    int maxRetryCount;
    boolean needsResumeOnReady = false;
    boolean waitingForCallback = false;
    boolean receiverRegistered;
    boolean callFinishAfterMaxRetry = false;
    private NetworkBatchListener<E, T> networkBatchListener;
    private Context context;
    private NetworkBroadcastReceiver networkBroadcastReceiver = new NetworkBroadcastReceiver();
    private PersistedBatchCallback<T> persistedBatchCallback = new PersistedBatchCallback<T>() {
        @Override
        public void onPersistFailure(T batch, Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getLocalizedMessage());
            }
        }

        @Override
        public void onPersistSuccess(final T batch) {
            //this will be called only once until finish is called.
            lastBatch = batch;
            registerReceiverIfRequired();
            resume();
        }

        @Override
        public void onFinish() {
            unregisterReceiver();
        }
    };

    public NetworkPersistedBatchReadyListener(final Context context, String filePath,
                                              SerializationStrategy<E, T> serializationStrategy,
                                              final Handler handler, NetworkBatchListener<E, T> listener,
                                              int maxRetryCount, int maxQueueSize, int trimToSize,
                                              int trimmingMode, TrimmedBatchCallback trimmedBatchCallback) {
        super(filePath, serializationStrategy, handler, maxQueueSize, trimToSize, trimmingMode, null, trimmedBatchCallback);
        this.context = context;
        this.networkBatchListener = listener;
        this.maxRetryCount = maxRetryCount;
        this.mCurrentTimeoutMs = defaultTimeoutMs;
        this.setListener(persistedBatchCallback);
    }

    public void setNetworkBatchListener(NetworkBatchListener<E, T> networkBatchListener) {
        this.networkBatchListener = networkBatchListener;
    }

    public void setCallFinishAfterMaxRetry(boolean callFinishAfterMaxRetry) {
        this.callFinishAfterMaxRetry = callFinishAfterMaxRetry;
    }

    public int getDefaultTimeoutMs() {
        return defaultTimeoutMs;
    }


    public void setDefaultTimeoutMs(int defaultTimeoutMs) {
        this.defaultTimeoutMs = defaultTimeoutMs;
        this.mCurrentTimeoutMs = defaultTimeoutMs;
    }

    public float getDefaultBackoffMultiplier() {
        return defaultBackoffMultiplier;
    }

    public void setDefaultBackoffMultiplier(float defaultBackoffMultiplier) {
        this.defaultBackoffMultiplier = defaultBackoffMultiplier;
    }

    void unregisterReceiver() {
        if (receiverRegistered) {
            context.unregisterReceiver(networkBroadcastReceiver);
            receiverRegistered = false;
            if (log.isDebugEnabled()) {
                log.debug("Unregistered network broadcast receiver {}", this);
            }
        }
    }

    void registerReceiverIfRequired() {
        if (!receiverRegistered) {
            //Register the broadcast receiver
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            context.registerReceiver(networkBroadcastReceiver, filter);
            receiverRegistered = true;
            if (log.isDebugEnabled()) {
                log.debug("Registered network broadcast receiver {}", this);
            }
        }
    }

    @Override
    public void onReady(BatchingStrategy<E, T> causingStrategy, T batch) {
        super.onReady(causingStrategy, batch);
        if (needsResumeOnReady) {
            needsResumeOnReady = false;
            resume();
        }
    }


    boolean isConnectedToNetwork() {
        return networkBatchListener.isNetworkConnected(context);
    }

    void makeNetworkRequest(final T batch, boolean isRetry) {
        if (isConnectedToNetwork()) {
            if (log.isDebugEnabled()) {
                log.debug("Performing network request for batch : {}, listener {}", batch, this);
            }
            if (!isRetry) {
                resetRetryCounters();
            }
            waitingForCallback = true;
            networkBatchListener.performNetworkRequest(batch, new ValueCallback<NetworkRequestResponse>() {
                @Override
                public void onReceiveValue(final NetworkRequestResponse value) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            waitingForCallback = false;
                            if (log.isDebugEnabled()) {
                                log.debug("callback received for {}", this);
                            }
                            if (!value.complete || (value.httpErrorCode >= HTTP_SERVER_ERROR_CODE_RANGE_START && value.httpErrorCode <= HTTP_SERVER_ERROR_CODE_RANGE_END)) {
                                retryCount++;
                                if (retryCount < maxRetryCount) {
                                    int backOff = exponentialBackOff();
                                    if (log.isDebugEnabled()) {
                                        log.debug("Request failed complete = {}, errorCode = {}, Retrying network request for batch {} after {} ms", value.complete, value.httpErrorCode, batch, backOff);
                                    }
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            makeNetworkRequest(batch, true);
                                        }
                                    }, backOff);
                                } else {
                                    if (log.isDebugEnabled()) {
                                        log.debug("Maximum network retry reached for {}", filePath);
                                    }
                                    if (callFinishAfterMaxRetry) {
                                        callFinishWithBatch(batch);
                                    } else {
                                        needsResumeOnReady = true;
                                    }
                                }
                            } else {
                                //all well :) now we go to next batch
                                finish(batch);
                            }
                        }
                    });

                }
            });
        } else {
            // this means we need a broadcast to resume the flow
            resetRetryCounters();
            waitingForCallback = false;
            needsResumeOnReady = true;
        }
    }

    public boolean callFinishWithBatch(T batch) {
        finish(batch);
        return true;
    }

    private void resetRetryCounters() {
        retryCount = 0;
        mCurrentTimeoutMs = defaultTimeoutMs;
    }

    /**
     * Backoff time to retry the batch for 5XX Server errors.
     *
     * @return new timeOut
     */
    int exponentialBackOff() {
        int timeOut = mCurrentTimeoutMs;
        mCurrentTimeoutMs += (mCurrentTimeoutMs * defaultBackoffMultiplier);
        return timeOut;
    }

    @Override
    public void finish(T batch) {
        retryCount = 0;
        lastBatch = null;
        super.finish(batch);
    }

    void resume() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!waitingForCallback && isConnectedToNetwork() && lastBatch != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Resuming = {}, networkConnected = {}, lastBatch = {}", waitingForCallback, isConnectedToNetwork(), lastBatch);
                    }
                    makeNetworkRequest(lastBatch, false);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Wont resume since waiting = {}, networkConnected = {}, lastBatch = {}", waitingForCallback, isConnectedToNetwork(), lastBatch);
                    }
                }
            }
        });

    }

    public static abstract class NetworkBatchListener<E extends Data, T extends Batch<E>> {

        /**
         * Implement this method and make your network request here. Once request is complete, call the {@link ValueCallback#onReceiveValue(Object)} method.
         * This method will be called once the batch has been persisted. The batch will be removed or retried once you invoke the networkBatchListener.
         * While invoking the networkBatchListener, pass a {@link NetworkRequestResponse} object with the following data.
         * If the network response was successfully received, set complete to true, and set httpErrorCode to the status code from server. If status code is 5XX, this batch will be retried. If status code is 200 or 4XX the batch will be discarded and next batch will be processed.
         * If the network response was not received (timeout or not connected or any other network error), set complete to false. This will cause a retry until max retries are reached.
         * <p>
         * Note: If there is a network redirect, do not call the networkBatchListener, and wait for the final redirected response and pass that one.
         *
         * @param batch    batch of data
         * @param callback callback
         */
        public abstract void performNetworkRequest(final T batch, final ValueCallback<NetworkRequestResponse> callback);

        /**
         * @return true if network is connected
         */
        public boolean isNetworkConnected(Context context) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return null != networkInfo && networkInfo.isConnected();
        }
    }

    public static class NetworkRequestResponse {
        public boolean complete; //indicates whether a network response was received.
        public int httpErrorCode;

        public NetworkRequestResponse(boolean isComplete, int httpErrorCode) {
            this.complete = isComplete;
            this.httpErrorCode = httpErrorCode;
        }
    }

    public class NetworkBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (log.isDebugEnabled()) {
                log.debug("Got network broadcast, resuming operations {}", NetworkPersistedBatchReadyListener.this);
            }
            resume();
        }
    }
}