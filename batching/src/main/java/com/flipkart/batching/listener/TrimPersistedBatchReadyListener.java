package com.flipkart.batching.listener;

import android.os.Handler;

import com.flipkart.batching.Batch;
import com.flipkart.batching.BatchingStrategy;
import com.flipkart.batching.Data;
import com.flipkart.batching.persistence.SerializationStrategy;

import org.slf4j.LoggerFactory;


/**
 * TrimPersistedBatchReadyListener that extends {@link PersistedBatchReadyListener}
 */
public class TrimPersistedBatchReadyListener<E extends Data, T extends Batch<E>> extends PersistedBatchReadyListener<E, T> {
    public final static int MODE_TRIM_NONE = 0;
    public final static int MODE_TRIM_AT_START = 1;
    public final static int MODE_TRIM_ON_READY = 1 << 1;
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(TrimPersistedBatchReadyListener.class);
    protected final Handler handler;
    private final TrimmedBatchCallback trimListener;
    private int trimSize, maxQueueSize;
    private int mode;

    public TrimPersistedBatchReadyListener(String filePath, SerializationStrategy<E, T> serializationStrategy, Handler handler, int maxQueueSize, int trimSize, int mode, PersistedBatchCallback<T> persistedBatchCallback, TrimmedBatchCallback trimmedBatchCallback) {
        super(filePath, serializationStrategy, handler, persistedBatchCallback);
        if (trimSize > maxQueueSize) {
            throw new IllegalArgumentException("trimSize must be smaller than maxQueueSize");
        }
        this.trimSize = trimSize;
        this.maxQueueSize = maxQueueSize;
        this.handler = handler;
        this.mode = mode;
        this.trimListener = trimmedBatchCallback;
    }

    @Override
    protected void onInitialized() {
        super.onInitialized();
        if ((mode & MODE_TRIM_AT_START) == MODE_TRIM_AT_START) {
            trimQueue();
        }
    }

    @Override
    public void onReady(BatchingStrategy<E, T> causingStrategy, T batch) {
        super.onReady(causingStrategy, batch);
        handler.post(new Runnable() {
            @Override
            public void run() {
                if ((mode & MODE_TRIM_ON_READY) == MODE_TRIM_ON_READY) {
                    trimQueue();
                }
            }
        });
    }

    private void trimQueue() {
        int oldSize = getSize();
        if (oldSize >= maxQueueSize && remove(trimSize)) {
            remove(trimSize);
            callTrimListener(oldSize, getSize());
        }
    }

    private void callTrimListener(int oldSize, int newSize) {
        if (trimListener != null) {
            trimListener.onTrimmed(oldSize, newSize);
        }
    }
}
