package com.flipkart.batching.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.webkit.ValueCallback;

import com.flipkart.batching.Batch;
import com.flipkart.batching.BatchingStrategy;
import com.flipkart.batching.Data;
import com.flipkart.batching.persistence.SerializationStrategy;
import com.squareup.tape.QueueFile;

import java.io.File;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by kushal.sharma on 29/02/16 at 11:58 AM.
 */
@Slf4j
public class NetworkPersistedBatchReadyListener<E extends Data, T extends Batch<E>> extends PersistedBatchReadyListener<E, T> {

    private static final int HTTP_SERVER_ERROR_CODE_RANGE_START = 500;
    private static final int HTTP_SERVER_ERROR_CODE_RANGE_END = 599;
    public int defaultTimeoutMs = 2500;
    public float defaultBackoffMultiplier = 1f;
    private NetworkBatchListener<E, T> networkBatchListener;
    private Context context;
    private T lastBatch;
    private int retryCount = 0;
    private int mCurrentTimeoutMs = 0;
    private int maxRetryCount;
    private NetworkBroadcastReceiver networkBroadcastReceiver = new NetworkBroadcastReceiver();
    private boolean retryLimitReached = false;
    private boolean waitingForCallback = false;
    private boolean receiverRegistered;
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
            handler.post(new Runnable() {
                    @Override
                    public void run() {
                        makeNetworkRequest(batch, false);
                    }
                });
        }

        @Override
        public void onFinish() {
            unregisterReceiver();
        }
    };

    private void unregisterReceiver() {
        if(receiverRegistered) {
            context.unregisterReceiver(networkBroadcastReceiver);
            receiverRegistered = false;
        }
    }

    private void registerReceiverIfRequired() {
        if (!receiverRegistered) {
            //Register the broadcast receiver
            IntentFilter filter = new IntentFilter();
            filter.addAction(Context.CONNECTIVITY_SERVICE);
            context.registerReceiver(networkBroadcastReceiver, filter); //todo, does calling this multple times cause duplicate broadcasts
            receiverRegistered = true;
        }
    }

    public NetworkPersistedBatchReadyListener(final Context context, File file, SerializationStrategy<E, T> serializationStrategy, final Handler handler, NetworkBatchListener<E, T> listener, int maxRetryCount) {
        super(file, serializationStrategy, handler, null);
        this.context = context;
        this.networkBatchListener = listener;
        this.maxRetryCount = maxRetryCount;
        this.mCurrentTimeoutMs = defaultTimeoutMs;
        this.setListener(persistedBatchCallback);
    }

    public void setDefaultTimeoutMs(int defaultTimeoutMs) {
        this.defaultTimeoutMs = defaultTimeoutMs;
    }

    public void setDefaultBackoffMultiplier(float defaultBackoffMultiplier) {
        this.defaultBackoffMultiplier = defaultBackoffMultiplier;
    }

    @Override
    public void onReady(BatchingStrategy<E, T> causingStrategy, T batch) {
        super.onReady(causingStrategy, batch);
        if (retryLimitReached) {
            retryLimitReached = false;
            resume();
        }

    }

    @Override
    protected void onInitialized(QueueFile queueFile) {
        super.onInitialized(queueFile);
    }

    private boolean isConnectedToNetwork() {
        return networkBatchListener.isNetworkConnected(context);
    }

    private void makeNetworkRequest(final T batch, boolean isRetry) {
        if (isConnectedToNetwork()) {
            if (log.isDebugEnabled()) {
                log.debug("Performing network request for batch : {}", batch);
            }
            if (!isRetry) {
                resetRetryCounters();
            }
            waitingForCallback = true;
            networkBatchListener.performNetworkRequest(batch, new ValueCallback<NetworkRequestResponse>() {
                @Override
                public void onReceiveValue(NetworkRequestResponse value) {
                    waitingForCallback = false;
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
                                log.debug("Maximum network retry reached for {}", getQueueFile());
                            }
                            retryLimitReached = true;
                        }
                    } else {
                        //all well :) now we go to next batch
                        finish(batch);
                    }
                }
            });
        } else {
            // this means we need a broadcast to resume the flow
            resetRetryCounters();
            waitingForCallback = false;
        }
    }

    private void resetRetryCounters() {
        retryCount = 0;
        mCurrentTimeoutMs = defaultTimeoutMs;
    }

    /**
     * Backoff time to retry the batch for 5XX Server errors.
     *
     * @return
     */
    private int exponentialBackOff() {
        mCurrentTimeoutMs += (mCurrentTimeoutMs * defaultBackoffMultiplier);
        return mCurrentTimeoutMs;
    }

    @Override
    public void finish(T batch) {
        retryCount = 0;
        lastBatch = null;
        super.finish(batch);
    }

    private void resume() {
        if (!waitingForCallback && isConnectedToNetwork() && lastBatch != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    makeNetworkRequest(lastBatch, false);
                }
            });
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Wont resume since waiting = {}, networkConnected = {}, lastBatch = {}", waitingForCallback, isConnectedToNetwork(), lastBatch);
            }
        }
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
         * @param batch
         * @param callback
         */
        abstract void performNetworkRequest(final T batch, final ValueCallback<NetworkRequestResponse> callback);

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
        public NetworkRequestResponse(boolean isComplete, int httpErrorCode){
            this.complete = isComplete;
            this.httpErrorCode = httpErrorCode;
        }
    }

    public class NetworkBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (log.isDebugEnabled()) {
                log.debug("Got network broadcast, resuming operations");
            }
            resume();
        }
    }
}