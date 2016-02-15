package com.flipkart.batching;

import android.content.Context;
import android.os.Handler;

import com.flipkart.data.Data;
import com.flipkart.persistence.PersistenceStrategy;

import java.util.Collection;

/**
 * TimeBatchingStrategy extends abstract class {@link BaseBatchingStrategy} which is an
 * implementation of {@link BatchingStrategy}. This class takes timeOut and persistenceStrategy
 * as parameters in constructor. This strategy persist data according to the provided
 * {@link PersistenceStrategy}, starts/reset the timer whenever {@link Data} objects are pushed
 * and calls {@link #onReadyListener} when timeOut happens.
 */

public class TimeBatchingStrategy extends BaseBatchingStrategy {
    private long timeOut;
    private Handler handler;

    public TimeBatchingStrategy(long timeOut, PersistenceStrategy persistenceStrategy) {
        super(persistenceStrategy);
        if (timeOut <= 0) {
            throw new IllegalArgumentException("TimeOut duration should be greater than 0");
        } else {
            this.timeOut = timeOut;
        }
    }

    @Override
    public void flush(boolean forced) {
        Collection<Data> data = getPersistenceStrategy().getData();
        if (forced) {
            if (!data.isEmpty()) {
                getOnReadyListener().onReady(data);
                getPersistenceStrategy().removeData(data);
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
    public void onInitialized(BatchController controller, Context context, OnBatchReadyListener onBatchReadyListener, Handler handler) {
        super.onInitialized(controller, context, onBatchReadyListener, handler);
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

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            flush(true);
        }
    };
}

