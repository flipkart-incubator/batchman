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
import com.flipkart.batching.BatchImpl;
import com.flipkart.batching.BatchingStrategy;
import com.flipkart.batching.Data;
import com.flipkart.batching.OnBatchReadyListener;
import com.flipkart.batching.persistence.PersistenceStrategy;
import com.google.gson.annotations.SerializedName;

import java.util.Collection;

/**
 * TimeBatchingStrategy extends abstract class {@link BaseBatchingStrategy} which is an
 * implementation of {@link BatchingStrategy}. This class takes timeOut and persistenceStrategy
 * as parameters in constructor. It persist data according to the provided
 * {@link PersistenceStrategy}, starts/reset the timer whenever {@link Data} objects are pushed
 * and calls {@link #onReadyListener} when timeOut happens.
 */
public class TimeBatchingStrategy<E extends Data> extends BaseBatchingStrategy<E, TimeBatchingStrategy.TimeBatch<E>> {
    private long timeOut;
    private Handler handler;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            flush(true);
        }
    };

    /**
     * This constructor takes timeOut and {@link PersistenceStrategy} as parameters in
     * constructor. Also, throws a IllegalArgumentException if {@link #timeOut} is less than
     * or equal to 0.
     *
     * @param timeOut             time out
     * @param persistenceStrategy persistence strategy
     */
    public TimeBatchingStrategy(long timeOut, PersistenceStrategy<E> persistenceStrategy) {
        super(persistenceStrategy);
        if (timeOut <= 0) {
            throw new IllegalArgumentException("TimeOut duration should be greater than 0");
        } else {
            this.timeOut = timeOut;
        }
    }

    /**
     * Calls {@link #onReadyListener} with batched data.
     *
     * @param forced set true if forced
     */
    @Override
    public void flush(boolean forced) {
        Collection<E> data = getPersistenceStrategy().getData();
        if (forced) {
            if (!data.isEmpty()) {
                getPersistenceStrategy().removeData(data);
                getOnReadyListener().onReady(this, new TimeBatch(data, timeOut));
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
    public void onInitialized(Context context, OnBatchReadyListener<E, TimeBatch<E>> onBatchReadyListener, Handler handler) {
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

    public static class TimeBatch<D extends Data> extends BatchImpl<D> {
        @SerializedName("timeOut")
        private long timeOut;

        public TimeBatch(Collection<D> dataCollection, long timeOut) {
            super(dataCollection);
            this.timeOut = timeOut;
        }

        public long getTimeOut() {
            return timeOut;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof TimeBatch) {
                return ((TimeBatch) o).getTimeOut() == timeOut && super.equals(o);
            }
            return super.equals(o);
        }

        @Override
        public int hashCode() {
            return 31 * super.hashCode() + Long.valueOf(timeOut).hashCode();
        }
    }
}

