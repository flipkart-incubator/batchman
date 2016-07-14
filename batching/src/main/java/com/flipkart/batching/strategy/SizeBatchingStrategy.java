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


    @Override
    public void onDataPushed(Collection<E> dataCollection) {
        super.onDataPushed(dataCollection);
        currentBatchSize = getPersistenceStrategy().getDataSize();
    }

    @Override
    public void flush(boolean forced) {
        currentBatchSize = getPersistenceStrategy().getDataSize();
        if ((forced || isBatchReady()) && currentBatchSize > 0) {
            Collection<E> data = getPersistenceStrategy().getData();
            getPersistenceStrategy().removeData(data);
            getOnReadyListener().onReady(this, new SizeBatch<E>(data, maxBatchSize));
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

        @Override
        public int hashCode() {
            return 31 * super.hashCode() + maxBatchSize;
        }
    }
}
