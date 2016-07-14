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
import com.flipkart.batching.Data;
import com.flipkart.batching.OnBatchReadyListener;
import com.flipkart.batching.persistence.PersistenceStrategy;
import com.google.gson.annotations.SerializedName;

import java.util.Collection;

/**
 * Created by kushal.sharma on 18/04/16.
 * Time Size Batching Strategy
 */
public class SizeTimeBatchingStrategy<E extends Data> extends BaseBatchingStrategy<E, SizeTimeBatchingStrategy.SizeTimeBatch<E>> {
    private int currentBatchSize;
    private int maxBatchSize;
    private long timeOut;
    private Handler handler;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            flush(true);
        }
    };

    public SizeTimeBatchingStrategy(PersistenceStrategy<E> persistenceStrategy, int maxBatchSize, long timeOut) {
        super(persistenceStrategy);
        if (maxBatchSize <= 0 || timeOut <= 0) {
            throw new IllegalStateException("Max. batch size and timeout duration should be greater than 0.");
        } else {
            this.maxBatchSize = maxBatchSize;
            this.timeOut = timeOut;
        }
    }

    @Override
    public void onDataPushed(Collection<E> dataCollection) {
        super.onDataPushed(dataCollection);
        currentBatchSize = getPersistenceStrategy().getDataSize();
    }

    /**
     * Returns true if currentBatch reaches the defined maxBatchSize.
     *
     * @return boolean type batch ready state
     */
    private boolean isBatchSizeReady() {
        return currentBatchSize >= maxBatchSize;
    }

    @Override
    public void flush(boolean forced) {
        Collection<E> data = getPersistenceStrategy().getData();
        currentBatchSize = data.size();

        if ((forced || isBatchSizeReady()) && currentBatchSize > 0) {
            getPersistenceStrategy().removeData(data);
            getOnReadyListener().onReady(this, new SizeTimeBatch<E>(data, maxBatchSize, timeOut));
        } else if (forced) {
            if (!data.isEmpty()) {
                getPersistenceStrategy().removeData(data);
                getOnReadyListener().onReady(this, new SizeTimeBatch<E>(data, maxBatchSize, timeOut));
            }
            stopTimer();
        } else {
            stopTimer();
            if (data.size() > 0) {
                startTimer();
            }
        }
    }

    @Override
    public void onInitialized(Context context, OnBatchReadyListener<E, SizeTimeBatch<E>> onBatchReadyListener, Handler handler) {
        super.onInitialized(context, onBatchReadyListener, handler);
        this.handler = handler;
    }

    /**
     * This method starts the timer.
     */
    private void startTimer() {
        handler.postDelayed(runnable, timeOut);
    }

    /**
     * This method stops the timer.
     */
    private void stopTimer() {
        handler.removeCallbacks(runnable);
    }

    public static class SizeTimeBatch<T extends Data> extends Batch<T> {
        @SerializedName("maxBatchSize")
        private int maxBatchSize;
        @SerializedName("timeOut")
        private long timeOut;

        public SizeTimeBatch(Collection dataCollection, int maxBatchSize, long timeOut) {
            super(dataCollection);
            this.maxBatchSize = maxBatchSize;
            this.timeOut = timeOut;
        }

        public int getMaxBatchSize() {
            return maxBatchSize;
        }

        public long getTimeOut() {
            return timeOut;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof SizeTimeBatch) {
                return (((SizeTimeBatch) o).getMaxBatchSize() == maxBatchSize
                        && ((SizeTimeBatch) o).getTimeOut() == timeOut
                        && super.equals(o));

            }
            return super.equals(o);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + maxBatchSize;
            result = 31 * result + Long.valueOf(timeOut).hashCode();
            return result;
        }
    }
}
