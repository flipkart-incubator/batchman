package com.flipkart.batching;

import android.content.Context;
import android.os.Handler;

import com.flipkart.data.Data;

import java.util.Collection;

/**
 * Created by kushal.sharma on 09/02/16.
 */
public class ComboBatchingStrategy implements BatchingStrategy {

    private String simpleClassName = this.getClass().getSimpleName();

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
