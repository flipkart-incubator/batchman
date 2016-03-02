package com.flipkart.batching.strategy;

import android.content.Context;
import android.os.Handler;

import com.flipkart.batching.Batch;
import com.flipkart.batching.BatchController;
import com.flipkart.batching.BatchingStrategy;
import com.flipkart.batching.Data;
import com.flipkart.batching.OnBatchReadyListener;
import com.flipkart.batching.persistence.PersistenceStrategy;

import java.util.Collection;

/**
 * This abstract class implements {@link BatchingStrategy} interface. BaseBatchingStrategy
 * hold an instance of provided {@link PersistenceStrategy}, {@link OnBatchReadyListener},
 * {@link BatchController} and {@link Context}.
 * <p/>
 * A class extending BaseBatchingStrategy must call super from it's constructor,
 * {@link #onDataPushed(Collection)} and {@link BatchingStrategy#onInitialized(Context, OnBatchReadyListener, Handler)} methods.
 */

public abstract class BaseBatchingStrategy<E extends Data, T extends Batch<E>> implements BatchingStrategy<E, T> {
    private Context context;
    private OnBatchReadyListener<E, T> onReadyListener;
    private PersistenceStrategy<E> persistenceStrategy;
    private boolean initialized = false;

    public BaseBatchingStrategy(PersistenceStrategy<E> persistenceStrategy) {
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
    public void onDataPushed(Collection<E> dataCollection) {
        persistenceStrategy.add(dataCollection);
    }

    @Override
    public abstract void flush(boolean forced);

    @Override
    public void onInitialized(Context context,
                              OnBatchReadyListener<E, T> onBatchReadyListener, Handler handler) {
        this.initialized = true;
        this.onReadyListener = onBatchReadyListener;
        this.context = context;
        this.persistenceStrategy.onInitialized();
    }

    public Context getContext() {
        return context;
    }

    public OnBatchReadyListener<E, T> getOnReadyListener() {
        return onReadyListener;
    }

    public PersistenceStrategy<E> getPersistenceStrategy() {
        return persistenceStrategy;
    }
}