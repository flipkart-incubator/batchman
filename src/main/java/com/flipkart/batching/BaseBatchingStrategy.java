package com.flipkart.batching;

import android.content.Context;
import android.os.Handler;

import com.flipkart.data.Data;
import com.flipkart.persistence.PersistenceStrategy;

import java.util.Collection;

public abstract class BaseBatchingStrategy implements BatchingStrategy {

    private PersistenceStrategy persistenceStrategy;
    private OnBatchReadyListener onReadyListener;
    private BatchController batchController;
    private Context context;

    public BaseBatchingStrategy(PersistenceStrategy persistenceStrategy) {
        if (persistenceStrategy != null) {
            this.persistenceStrategy = persistenceStrategy;
        } else {
            throw new IllegalArgumentException("Persistence Strategy cannot be null.");
        }
    }

    public Context getContext() {
        return context;
    }

    public OnBatchReadyListener getOnReadyListener() {
        return onReadyListener;
    }

    public PersistenceStrategy getPersistenceStrategy() {
        return persistenceStrategy;
    }

    public BatchController getBatchController() {
        return batchController;
    }

    @Override
    public void onDataPushed(Collection<Data> dataCollection) {
        persistenceStrategy.add(dataCollection);
    }

    @Override
    public abstract void flush(boolean forced);

    @Override
    public void onInitialized(BatchController controller, Context context, OnBatchReadyListener onBatchReadyListener, Handler handler) {
        this.onReadyListener = onBatchReadyListener;
        this.batchController = controller;
        this.context = context;
    }

}