package com.flipkart.batching;

import android.content.Context;
import android.os.Handler;

import java.util.Collection;

/**
 * Interface class for BatchingStrategy. An implementation of BatchingStrategy must
 * implement this interface and override all it's methods.
 */

public interface BatchingStrategy<E extends Data, T extends Batch<E>> {

    /**
     * This method tells the BatchingStrategy about added data. This method should send the
     * provided {@link Collection} of {@link Data} objects to the provided implementation of
     * {@link com.flipkart.batching.persistence.PersistenceStrategy}.
     *
     * @param dataCollection collection of {@link Data} objects
     */

    void onDataPushed(Collection<E> dataCollection);

    /**
     * This method fires the {@link OnBatchReadyListener} when a batch is ready, depending on the
     * provided BatchingStrategy.
     *
     * @param forced boolean type if isForced
     */

    void flush(boolean forced);

    /**
     * This method returns false if {@link Context}, {@link BatchController}, {@link OnBatchReadyListener}
     * and {@link Handler} are not initialized and true if initialized. Typically, onInitialized should
     * be called only once and the value of isInitialized must be set to true after initializing everything.
     *
     * @return boolean, true if initialized and false if not
     */

    boolean isInitialized();

    /**
     * Instance of {@link BatchController}, {@link Context}, {@link OnBatchReadyListener},
     * and {@link Handler} from {@link BatchController}.
     * @param context              context
     * @param onBatchReadyListener instance of {@link OnBatchReadyListener}
     * @param handler              instance of {@link Handler}
     */

    void onInitialized(Context context, OnBatchReadyListener<E, T> onBatchReadyListener, Handler handler);
}
