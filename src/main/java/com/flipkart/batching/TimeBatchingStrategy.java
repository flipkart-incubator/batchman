package com.flipkart.batching;

import android.content.Context;
import android.os.Handler;

import com.flipkart.data.Data;
import com.flipkart.persistence.PersistenceStrategy;

import java.util.Collection;

/**
 * Created by kushal.sharma on 09/02/16.
 */
public class TimeBatchingStrategy extends BaseBatchingStrategy {
    private String simpleClassName = this.getClass().getSimpleName();
    private long timeOut;
    private Handler handler;

    public TimeBatchingStrategy(long time, PersistenceStrategy persistenceStrategy) {
        super(persistenceStrategy);
        if (time <= 0) {
            throw new IllegalArgumentException("TIME OUT duration should be greater than 0");
        } else {
            timeOut = time;
        }
    }

    @Override
    public void flush(boolean forced) {
        Collection<Data> data = getPersistenceStrategy().getData();
        if (forced) {
            if (!data.isEmpty()) {
                getPersistenceStrategy().removeData(data);
                getOnReadyListener().onReady(data);
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

    private void startTimer() {
        handler.postDelayed(runnable, timeOut);
    }

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

