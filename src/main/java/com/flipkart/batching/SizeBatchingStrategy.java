package com.flipkart.batching;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

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
        this.maxBatchSize = maxBatchSize;
    }

    @Override
    public void onDataPushed(Collection<Data> dataCollection) {
        super.onDataPushed(dataCollection);
        refreshBatchDataAndCurrentBatchSize();
    }

    @Override
    public void flush(boolean forced) {
        refreshBatchDataAndCurrentBatchSize();
        if (forced || isBatchReady()) {
            removeBatchedDataAndFireOnReady();
        }
    }

    @Override
    public void onInitialized(BatchController controller, Context context, OnBatchReadyListener onBatchReadyListener, Handler handler) {
        super.onInitialized(controller, context, onBatchReadyListener, handler);
    }

    boolean isBatchReady() {
        return currentBatchSize >= maxBatchSize;
    }

    void refreshBatchDataAndCurrentBatchSize() {
        currentBatchSize = getPersistenceStrategy().getData().size();
    }

    void removeBatchedDataAndFireOnReady() {
        Collection<Data> data = getPersistenceStrategy().getData();
        getOnReadyListener().onReady(data);
        getPersistenceStrategy().removeData(data);
        Log.e("DatList Size", data.size() + "");
    }
}
