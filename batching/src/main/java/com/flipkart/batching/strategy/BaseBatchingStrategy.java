/*
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2016 Flipkart Internet Pvt. Ltd.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package com.flipkart.batching.strategy;

import android.content.Context;
import android.os.Handler;

import com.flipkart.batching.BatchController;
import com.flipkart.batching.BatchingStrategy;
import com.flipkart.batching.OnBatchReadyListener;
import com.flipkart.batching.persistence.PersistenceStrategy;
import com.flipkart.batching.core.Batch;
import com.flipkart.batching.core.Data;

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