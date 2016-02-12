package com.flipkart.batching;

import android.content.Context;
import android.os.Handler;

import com.flipkart.data.Data;

import java.util.Collection;

/**
 * ComboBatchingStrategy is an implementation of {@link BatchingStrategy}.
 * This class takes an array of {@link BatchingStrategy} as parameter in constructor and
 * calls {@link #onInitialized(BatchController, Context, OnBatchReadyListener, Handler)},
 * {@link #onDataPushed(Collection)} and {@link #flush(boolean)} on all the provided
 * batching strategies.
 * <p/>
 * A use case will be where we want to batch data when maxBatchSize is reached or there is a
 * timeOut. So, we will pass an instance of {@link SizeBatchingStrategy} as well as
 * {@link TimeBatchingStrategy} to this class with desired maxBatchSize and timeOut.
 *
 * @see BatchingStrategy
 * @see BaseBatchingStrategy
 * @see SizeBatchingStrategy
 * @see TimeBatchingStrategy
 * @see TagBatchingStrategy
 */

public class ComboBatchingStrategy implements BatchingStrategy {
    private BatchingStrategy[] batchingStrategies;

    public ComboBatchingStrategy(BatchingStrategy... batchingStrategies) {
        this.batchingStrategies = batchingStrategies;
    }

    @Override
    public void onInitialized(BatchController controller, Context context, OnBatchReadyListener onBatchReadyListener, Handler handler) {
        for (BatchingStrategy batchingStrategy : batchingStrategies) {
            batchingStrategy.onInitialized(controller, context, onBatchReadyListener, handler);
        }
    }

    @Override
    public void onDataPushed(Collection<Data> dataCollection) {
        for (BatchingStrategy batchingStrategy : batchingStrategies) {
            batchingStrategy.onDataPushed(dataCollection);
        }
    }

    @Override
    public void flush(boolean forced) {
        for (BatchingStrategy batchingStrategy : batchingStrategies) {
            batchingStrategy.flush(forced);
        }
    }
}
