package com.flipkart.batching;

import android.content.Context;
import android.os.Handler;

import com.flipkart.data.Data;

import java.util.Collection;

/**
 * Interface class for BatchingStrategy. A custom implementation of BatchingStrategy must
 * implement this interface and override all it's methods.
 * <p/>
 * BatchingStrategy provides an interface to access callback when data is pushed for making a
 * batch and to initialize {@link BatchController} and Context.
 */

public interface BatchingStrategy {
    /**
     * This callback is called when dataCollection is pushed to a BatchManager that implements
     * {@link BatchController}.
     * <p/>
     * The logic for making batches resides here. fireOnReadyCallback is called if the batch
     * is ready to serve. Also, dataCollection that is already served to the client must be removed here.
     *
     * @param dataCollection
     */

    void onDataPushed(Collection<Data> dataCollection);

    void flush(boolean forced);
    /**
     * Initialize the BatchController and Context to be used by the Batching Strategy.
     * @param controller instance of batchController
     * @param context    context
     * @param onBatchReadyListener
     * @param handler
     */

    void onInitialized(BatchController controller, Context context, OnBatchReadyListener onBatchReadyListener, Handler handler);

}
