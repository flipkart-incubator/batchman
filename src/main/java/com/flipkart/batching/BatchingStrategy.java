package com.flipkart.batching;

import android.content.Context;
import android.os.Handler;

import com.flipkart.data.Data;

import java.util.Collection;

/**
 * Interface class for BatchingStrategy. An implementation of BatchingStrategy must
 * implement this interface and override all it's methods.
 *
 */

public interface BatchingStrategy {

    /**
     * This method tells the BatchingStrategy about added data. This method should send the
     * provided {@link Collection} of {@link Data} objects to the provided implementation of
     * {@link com.flipkart.persistence.PersistenceStrategy}.
     *
     * @param dataCollection collection of {@link Data} objects
     */

    void onDataPushed(Collection<Data> dataCollection);

    /**
     * This method fires the {@link OnBatchReadyListener} when a batch is ready, depending on the
     * provided BatchingStrategy.
     *
     * @param forced boolean type if isForced
     */

    void flush(boolean forced);

    /**
     * Instance of {@link BatchController}, {@link Context}, {@link OnBatchReadyListener},
     * and {@link Handler} from {@link BatchController}.
     *
     * @param controller           instance of {@link BatchController}
     * @param context              context
     * @param onBatchReadyListener instance of {@link OnBatchReadyListener}
     * @param handler              instance of {@link Handler}
     */

    void onInitialized(BatchController controller, Context context, OnBatchReadyListener onBatchReadyListener, Handler handler);
}
