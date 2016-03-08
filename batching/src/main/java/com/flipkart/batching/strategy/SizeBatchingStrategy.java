package com.flipkart.batching.strategy;

import android.content.Context;
import android.os.Handler;

import com.flipkart.batching.Batch;
import com.flipkart.batching.BatchingStrategy;
import com.flipkart.batching.Data;
import com.flipkart.batching.OnBatchReadyListener;
import com.flipkart.batching.persistence.PersistenceStrategy;
import com.google.gson.annotations.SerializedName;

import java.util.Collection;

/**
 * SizeBatchingStrategy extends abstract class {@link BaseBatchingStrategy} which is an
 * implementation of {@link BatchingStrategy}. This class takes maxBatchSize and persistenceStrategy
 * as parameters in constructor. This strategy persist data according to the provided
 * {@link PersistenceStrategy} and calls {@link #onReadyListener} when the batch reaches the
 * maxBatchSize limit.
 */

public class SizeBatchingStrategy<E extends Data> extends BaseBatchingStrategy<E, SizeBatchingStrategy.SizeBatch<E>> {
    private int currentBatchSize;
    private int maxBatchSize;

    public SizeBatchingStrategy(int maxBatchSize, PersistenceStrategy<E> persistenceStrategy) {
        super(persistenceStrategy);
        currentBatchSize = 0;
        if (maxBatchSize <= 0) {
            throw new IllegalArgumentException("Max. Batch Size should be greater than 0");
        } else {
            this.maxBatchSize = maxBatchSize;
        }
    }

    public void setMaxBatchSize(int maxBatchSize) {
        this.maxBatchSize = maxBatchSize;
    }

    @Override
    public void onDataPushed(Collection<E> dataCollection) {
        super.onDataPushed(dataCollection);
        currentBatchSize = getPersistenceStrategy().getData().size();
    }

    @Override
    public void flush(boolean forced) {
        currentBatchSize = getPersistenceStrategy().getData().size();
        if ((forced || isBatchReady()) && currentBatchSize > 0) {
            Collection<E> data = getPersistenceStrategy().getData();
            getOnReadyListener().onReady(this, new SizeBatch<E>(data, maxBatchSize));
            getPersistenceStrategy().removeData(data);
        }
    }

    @Override
    public void onInitialized(Context context,
                              OnBatchReadyListener<E, SizeBatch<E>> onBatchReadyListener, Handler handler) {
        super.onInitialized(context, onBatchReadyListener, handler);
    }

    /**
     * Returns true if currentBatch reaches the defined maxBatchSize.
     *
     * @return boolean type batch ready state
     */
    protected boolean isBatchReady() {
        return currentBatchSize >= maxBatchSize;
    }

    public static class SizeBatch<T extends Data> extends Batch<T> {

        @SerializedName("maxBatchSize")
        private int maxBatchSize;

        public SizeBatch(Collection dataCollection, int maxBatchSize) {
            super(dataCollection);
            this.maxBatchSize = maxBatchSize;
        }

        public int getMaxBatchSize() {
            return maxBatchSize;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof SizeBatch) {
                return ((SizeBatch) o).getMaxBatchSize() == maxBatchSize && super.equals(o);
            }
            return super.equals(o);
        }
    }
}
