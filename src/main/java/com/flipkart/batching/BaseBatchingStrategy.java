package com.flipkart.batching;

import android.content.Context;
import android.os.Handler;

import com.flipkart.data.Data;
import com.flipkart.persistence.PersistenceStrategy;

import java.util.Collection;

/**
 * This abstract class implements {@link BatchingStrategy} interface. BaseBatchingStrategy
 * hold an instance of provided {@link PersistenceStrategy}, {@link OnBatchReadyListener},
 * {@link BatchController} and {@link Context}.
 * <p/>
 * A class extending BaseBatchingStrategy must call super from it's constructor,
 * {@link #onDataPushed(Collection)} and {@link #onInitialized(BatchController, Context,
 * OnBatchReadyListener, Handler)} methods.
 */

public abstract class BaseBatchingStrategy implements BatchingStrategy {
    private Context context;
    private BatchController batchController;
    private OnBatchReadyListener onReadyListener;
    private PersistenceStrategy persistenceStrategy;
    private boolean initialized = false;

    public BaseBatchingStrategy(PersistenceStrategy persistenceStrategy) {
        if (persistenceStrategy != null) {
            this.persistenceStrategy = persistenceStrategy;
        } else {
            throw new IllegalArgumentException("Persistence Strategy cannot be null.");
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void onDataPushed(Collection<Data> dataCollection) {
        persistenceStrategy.add(dataCollection);
    }

    @Override
    public abstract void flush(boolean forced);

    @Override
    public void onInitialized(BatchController controller, Context context,
                              OnBatchReadyListener onBatchReadyListener, Handler handler) {
        initialized = true;
        this.onReadyListener = onBatchReadyListener;
        this.batchController = controller;
        this.context = context;
        this.persistenceStrategy.onInitialized();
    }

    public Context getContext() {
        return context;
    }

    public BatchController getBatchController() {
        return batchController;
    }

    public OnBatchReadyListener getOnReadyListener() {
        return onReadyListener;
    }

    public PersistenceStrategy getPersistenceStrategy() {
        return persistenceStrategy;
    }


}