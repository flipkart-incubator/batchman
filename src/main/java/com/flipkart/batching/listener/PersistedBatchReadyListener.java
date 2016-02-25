package com.flipkart.batching.listener;

import android.os.Handler;

import com.flipkart.batching.Batch;
import com.flipkart.batching.BatchingStrategy;
import com.flipkart.batching.OnBatchReadyListener;
import com.flipkart.batching.Data;
import com.flipkart.batching.exception.DeserializeException;
import com.flipkart.batching.persistence.SerializationStrategy;
import com.squareup.tape.QueueFile;

import java.io.File;
import java.io.IOException;

/**
 * Created by kushal.sharma on 13/02/16.
 * Todo Document
 */

public abstract class PersistedBatchReadyListener<E extends Data, T extends Batch<E>> implements OnBatchReadyListener<E, T> {
    private final SerializationStrategy<E, T> serializationStrategy;
    private final File file;
    private final Handler handler;
    private QueueFile queueFile;

    private boolean initialized;
    private boolean isWaitingToFinish;

    public PersistedBatchReadyListener(File file, SerializationStrategy serializationStrategy, Handler handler) {
        this.file = file;
        this.serializationStrategy = serializationStrategy;
        this.handler = handler;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public abstract void onPersistSuccess(T batch);

    public abstract void onPersistFailure(T batch, Exception e);

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
                    onPersistFailure(batch, e);
                }
            }
        });
    }

    private void callPersistSuccess(T batch) {
        if (!isWaitingToFinish) {
            isWaitingToFinish = true;
            onPersistSuccess(batch);
        }
    }

    private void initializeIfRequired() {
        if (!initialized) {
            initialized = true;
            try {
                this.queueFile = new QueueFile(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void finish() {
        if (!queueFile.isEmpty()) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        queueFile.remove();
                    } catch (IOException e) {
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
