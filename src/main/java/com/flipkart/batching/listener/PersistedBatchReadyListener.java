package com.flipkart.batching.listener;

import android.os.Handler;

import com.flipkart.batching.Batch;
import com.flipkart.batching.BatchingStrategy;
import com.flipkart.batching.Data;
import com.flipkart.batching.OnBatchReadyListener;
import com.flipkart.batching.exception.DeserializeException;
import com.flipkart.batching.persistence.SerializationStrategy;
import com.squareup.tape.QueueFile;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

/**
 * Created by kushal.sharma on 13/02/16.
 * Todo Document
 */

public class PersistedBatchReadyListener<E extends Data, T extends Batch<E>> implements OnBatchReadyListener<E, T> {
    private final File file;
    private final Handler handler;
    private final PersistedBatchCallback<T> listener;
    private final SerializationStrategy<E, T> serializationStrategy;
    private QueueFile queueFile;
    private boolean initialized;
    private boolean isWaitingToFinish;

    public PersistedBatchReadyListener(File file, SerializationStrategy<E, T> serializationStrategy, Handler handler, @Nullable PersistedBatchCallback<T> listener) {
        this.file = file;
        this.serializationStrategy = serializationStrategy;
        this.handler = handler;
        this.listener = listener;
    }

    public PersistedBatchCallback getListener() {
        return listener;
    }

    protected QueueFile getQueueFile() {
        return queueFile;
    }

    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void onReady(BatchingStrategy<E, T> causingStrategy, final T batch) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                initializeIfRequired();
                try {
                    queueFile.add(serializationStrategy.serializeBatch(batch));
                    callPersistSuccess(batch);
                } catch (Exception e) {
                    callPersistFailure(batch, e);
                }
            }
        });
    }

    private void callPersistFailure(T batch, Exception e) {
        if (listener != null) {
            listener.onPersistFailure(batch, e);
        }
    }

    private void callPersistSuccess(T batch) {
        if (!isWaitingToFinish) {
            isWaitingToFinish = true;
            if (listener != null) {
                listener.onPersistSuccess(batch);
            }
        }
    }

    private void initializeIfRequired() {
        if (!initialized) {
            initialized = true;
            try {
                this.queueFile = new QueueFile(file);
                onInitialized(queueFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void onInitialized(QueueFile queueFile) {
    }

    public void finish(final T batch) {
        if (!queueFile.isEmpty()) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] peeked = queueFile.peek();
                        if (peeked != null) {
                            T peekedBatch = serializationStrategy.deserializeBatch(peeked);
                            if (batch != null && batch.equals(peekedBatch)) {
                                queueFile.remove();
                            }
                        }
                    } catch (IOException | DeserializeException e) {
                        e.printStackTrace();
                    }
                    isWaitingToFinish = false;
                    checkPending();
                }
            });
        }
    }

    private void checkPending() {
        initializeIfRequired();
        if (!queueFile.isEmpty()) {
            try {
                byte[] eldest = queueFile.peek();
                if (eldest != null) {
                    T batch = serializationStrategy.deserializeBatch(eldest);
                    callPersistSuccess(batch);
                }
            } catch (IOException | DeserializeException e) {
                e.printStackTrace();
            }
        }
    }
}
