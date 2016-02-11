package com.flipkart.batching;

import android.content.Context;
import android.os.Handler;

import com.flipkart.data.Data;
import com.flipkart.exception.IllegalArgumentException;
import com.flipkart.exception.PersistenceNullException;
import com.flipkart.persistence.PersistenceStrategy;

import java.util.Collection;

/**
 * SizeBatchingStrategy extends abstract class {@link BaseBatchingStrategy} which is an
 * implementation of {@link BatchingStrategy}. This class takes maxBatchSize and persistenceStrategy
 * as parameters in constructor. This strategy persist data according to the provided
 * {@link PersistenceStrategy} and calls {@link #onReadyListener} when the batch reaches the
 * maxBatchSize limit.
 */

public class SizeBatchingStrategy extends BaseBatchingStrategy {
    private int currentBatchSize;
    private int maxBatchSize;

    public SizeBatchingStrategy(int maxBatchSize, PersistenceStrategy persistenceStrategy)
            throws IllegalArgumentException, PersistenceNullException {
        super(persistenceStrategy);
        currentBatchSize = 0;
        if (maxBatchSize <= 0) {
            throw new IllegalArgumentException("Max. Batch Size should be greater than 0");
        } else {
            this.maxBatchSize = maxBatchSize;
        }
    }

    @Override
    public void onDataPushed(Collection<Data> dataCollection) {
        super.onDataPushed(dataCollection);
        currentBatchSize = getPersistenceStrategy().getData().size();
    }

    @Override
    public void flush(boolean forced) {
        currentBatchSize = getPersistenceStrategy().getData().size();
        if ((forced || isBatchReady()) && currentBatchSize > 0) {
            Collection<Data> data = getPersistenceStrategy().getData();
            getOnReadyListener().onReady(data);
            getPersistenceStrategy().removeData(data);
        }
    }

    @Override
    public void onInitialized(BatchController controller, Context context,
                              OnBatchReadyListener onBatchReadyListener, Handler handler) {
        super.onInitialized(controller, context, onBatchReadyListener, handler);
    }

    protected boolean isBatchReady() {
    /**
     * Returns true if currentBatch reaches the defined maxBatchSize.
     *
     * @return boolean type batch ready state
     */
    protected boolean isBatchReady() {
        return currentBatchSize >= maxBatchSize;
    }
}
