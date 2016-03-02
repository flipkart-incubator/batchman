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
import com.flipkart.batching.Data;
import com.flipkart.batching.persistence.SerializationStrategy;

import java.io.File;

/**
 * Created by kushal.sharma on 29/02/16.
 */
public class NetworkPersistedBatchReadyListener<E extends Data, T extends Batch<E>> extends PersistedBatchReadyListener<E, T> {

    INetworkBatchListener callback;
    Context context;
    T lastBatch;
    int retryCount = 0;
    int maxRetryCount;
    NetworkBroadcastReceiver networkBroadcastReceiver = new NetworkBroadcastReceiver();

    public NetworkPersistedBatchReadyListener(final Context context, File file, SerializationStrategy<E, T> serializationStrategy, Handler handler, INetworkBatchListener<E, T> listener, int maxRetryCount) {
        super(file, serializationStrategy, handler, null);
        this.context = context;
        this.callback = listener;
        this.maxRetryCount = maxRetryCount;
        this.setListener(new PersistedBatchCallback<T>() {
            @Override
            public void onPersistFailure(T batch, Exception e) {

            }

            @Override
            public void onPersistSuccess(T batch) {
                //Check network. If no network present
                lastBatch = batch;
                if (isConnectedToNetwork()) {
                    makeNetworkRequest(batch);
                } else {
                    //Register the broadcast receiver
                    IntentFilter filter = new IntentFilter();
                    filter.addAction(Context.CONNECTIVITY_SERVICE);
                    context.registerReceiver(networkBroadcastReceiver, filter);
                }

            }
        });
    }

    private boolean isConnectedToNetwork() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return null != networkInfo && networkInfo.isConnected();
    }

    private void makeNetworkRequest(final T batch) {
        callback.performNetworkRequest(batch, new ValueCallback<NetworkRequestResponse>() {
            @Override
            public void onReceiveValue(NetworkRequestResponse value) {
                if (!value.complete || (value.httpErrorCode >= 500 && value.httpErrorCode <= 599)) {
                    //Put this in a retry logic
                    retryCount++;
                    if (retryCount < 5) {
                        makeNetworkRequest(batch);
                    }

                } else {
                    retryCount = 0;
                    finish(batch);
                    context.unregisterReceiver(networkBroadcastReceiver);
                }
            }
        });
    }

    public interface INetworkBatchListener<E extends Data, T extends Batch<E>> {
        void performNetworkRequest(final T batch, final ValueCallback<NetworkRequestResponse> callback);
    }

    public static class NetworkRequestResponse {
        public boolean complete;
        public int httpErrorCode;
    }

    public class NetworkBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (isConnectedToNetwork()) {
                makeNetworkRequest(lastBatch);
            } else {
                //Check and re-register
                IntentFilter filter = new IntentFilter();
                filter.addAction(Context.CONNECTIVITY_SERVICE);
                context.registerReceiver(networkBroadcastReceiver, filter);
            }
        }
    }

}
