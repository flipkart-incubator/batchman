package com.flipkart.batching;

import android.content.Context;
import android.os.Handler;

import com.flipkart.data.Data;
import com.flipkart.persistence.PersistenceStrategy;

import java.util.Collection;

/**
 * Created by kushal.sharma on 07/02/16.
 * SizeBatchingStrategy
 */

public class SizeBatchingStrategy extends BaseBatchingStrategy {
    private int currentBatchSize;
    private int maxBatchSize;

    public SizeBatchingStrategy(int maxBatchSize, PersistenceStrategy persistenceStrategy) {
        super(persistenceStrategy);
        currentBatchSize = 0;

        if (maxBatchSize > 0) {
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
        if (forced || isBatchReady()) {
            Collection<Data> data = getPersistenceStrategy().getData();
            getOnReadyListener().onReady(data);
            getPersistenceStrategy().removeData(data);
        }
    }

    @Override
    public void onInitialized(BatchController controller, Context context, OnBatchReadyListener onBatchReadyListener, Handler handler) {
        super.onInitialized(controller, context, onBatchReadyListener, handler);
    }

    protected boolean isBatchReady() {
        return currentBatchSize >= maxBatchSize;
    }
}
