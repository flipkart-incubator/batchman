package com.flipkart.batching.strategy;

import android.content.Context;
import android.os.Handler;

import com.flipkart.batching.Batch;
import com.flipkart.batching.BatchingStrategy;
import com.flipkart.batching.Data;
import com.flipkart.batching.OnBatchReadyListener;
import com.flipkart.batching.persistence.PersistenceStrategy;

import java.util.Collection;

/**
 * TimeBatchingStrategy extends abstract class {@link BaseBatchingStrategy} which is an
 * implementation of {@link BatchingStrategy}. This class takes timeOut and persistenceStrategy
 * as parameters in constructor. This strategy persist data according to the provided
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

    public TimeBatchingStrategy(long timeOut, PersistenceStrategy<E> persistenceStrategy) {
        super(persistenceStrategy);
        if (timeOut <= 0) {
            throw new IllegalArgumentException("TimeOut duration should be greater than 0");
        } else {
            this.timeOut = timeOut;
        }
    }

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

    public static class TimeBatch<D extends Data> extends Batch<D> {
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
                return ((TimeBatch) o).getTimeOut() == timeOut;
            }
            return super.equals(o);
        }
    }
}

