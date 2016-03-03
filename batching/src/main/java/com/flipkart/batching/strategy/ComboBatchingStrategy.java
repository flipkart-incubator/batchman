package com.flipkart.batching.strategy;

import android.content.Context;
import android.os.Handler;

import com.flipkart.batching.Batch;
import com.flipkart.batching.BatchingStrategy;
import com.flipkart.batching.Data;
import com.flipkart.batching.OnBatchReadyListener;

import java.util.Collection;

/**
 * ComboBatchingStrategy is an implementation of {@link BatchingStrategy}.
 * This class takes an array of {@link BatchingStrategy} as parameter in constructor and
 * calls {@link BatchingStrategy#onInitialized(Context, OnBatchReadyListener, Handler)},
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

public class ComboBatchingStrategy<E extends Data, C extends Batch<E>> implements BatchingStrategy<E, ComboBatchingStrategy.ComboBatch<E>> {
    private BatchingStrategy<E, C>[] batchingStrategies;
    private boolean initialized = false;

    @SafeVarargs
    public ComboBatchingStrategy(BatchingStrategy<E, C>... batchingStrategies) {
        this.batchingStrategies = batchingStrategies;
    }

    @Override
    public void onInitialized(Context context, final OnBatchReadyListener<E, ComboBatch<E>> parentBatchReadyListener, Handler handler) {
        initialized = true;
        OnBatchReadyListener childBatchReadyListener = new OnBatchReadyListener<E, Batch<E>>() {
            @Override
            public void onReady(BatchingStrategy<E, Batch<E>> causingStrategy, Batch<E> batch) {
                parentBatchReadyListener.onReady(ComboBatchingStrategy.this, new ComboBatch(batch)); //this listener overrides the causing strategy
            }
        };
        for (BatchingStrategy<E, C> batchingStrategy : batchingStrategies) {
            batchingStrategy.onInitialized(context, childBatchReadyListener, handler);
        }
    }

    @Override
    public void onDataPushed(Collection<E> dataCollection) {
        for (BatchingStrategy<E, C> batchingStrategy : batchingStrategies) {
            batchingStrategy.onDataPushed(dataCollection);
        }
    }

    @Override
    public void flush(boolean forced) {
        for (BatchingStrategy batchingStrategy : batchingStrategies) {
            batchingStrategy.flush(forced);
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }


    public static class ComboBatch<T extends Data> extends Batch<T> {
        private Batch<T> batch;

        public ComboBatch(Batch<T> batch) {
            super(null);
            this.batch = batch;
        }

        @Override
        public Collection<T> getDataCollection() {
            return batch.getDataCollection();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof ComboBatch) {
                return ((ComboBatch) o).batch.equals(o);
            }
            return super.equals(o);
        }
    }
}
