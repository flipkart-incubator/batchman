package com.flipkart.batching.listener;

import android.os.Handler;

import com.flipkart.batching.Batch;
import com.flipkart.batching.BatchingStrategy;
import com.flipkart.batching.Data;
import com.flipkart.batching.OnBatchReadyListener;
import com.flipkart.batching.exception.DeserializeException;
import com.flipkart.batching.persistence.SerializationStrategy;
import com.flipkart.batching.tape.QueueFile;

import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;


/**
 * Created by kushal.sharma on 13/02/16.
 * Todo Persisted Batch Ready Listener Document
 */

public class PersistedBatchReadyListener<E extends Data, T extends Batch<E>> implements OnBatchReadyListener<E, T> {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(PersistedBatchReadyListener.class);
    protected final Handler handler;
    private final String filePath;
    private final SerializationStrategy<E, T> serializationStrategy;
    private PersistedBatchCallback<T> listener;
    private QueueFile queueFile;
    private boolean isWaitingToFinish;
    private T peekedBatch;

    public PersistedBatchReadyListener(String filePath, SerializationStrategy<E, T> serializationStrategy, Handler handler, @Nullable PersistedBatchCallback<T> listener) {
        this.filePath = filePath;
        this.serializationStrategy = serializationStrategy;
        this.handler = handler;
        this.listener = listener;
    }

    public PersistedBatchCallback getListener() {
        return listener;
    }

    public void setListener(PersistedBatchCallback<T> listener) {
        this.listener = listener;
    }

    public QueueFile getQueueFile() {
        return queueFile;
    }

    public void setQueueFile(QueueFile queueFile) {
        this.queueFile = queueFile;
    }

    public boolean isInitialized() {
        return queueFile != null;
    }

    @Override
    public void onReady(BatchingStrategy<E, T> causingStrategy, final T batch) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                initializeIfRequired();
                try {
                    queueFile.add(serializationStrategy.serializeBatch(batch));
                    checkPendingAndContinue();
                } catch (Exception e) {
                    if (log.isErrorEnabled()) {
                        log.error(e.getLocalizedMessage());
                    }
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
        if (!isInitialized()) {
            try {
                File file = new File(this.filePath);
                this.queueFile = new QueueFile(file);
                onInitialized(queueFile);
            } catch (IOException e) {
                if (log.isErrorEnabled()) {
                    log.error(e.getLocalizedMessage());
                }
            }
        }
    }

    public void close() {
        if (queueFile != null) {
            try {
                queueFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void onInitialized(QueueFile queueFile) {
    }

    public void finish(final T batch) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!queueFile.isEmpty()) {
                    try {
                        if (peekedBatch != null) {
                            if (batch != null && batch.equals(peekedBatch)) {
                                queueFile.remove();
                            } else {
                                // We are currently seeing this very very rarely. We want to get more info here before we throw this exception
                                if (log.isErrorEnabled()) {
                                    log.error("Finish was called with a different batch, expected " + peekedBatch + " was " + batch);
                                }
                                //throw new IllegalStateException("Finish was called with a different batch, expected " + peekedBatch + " was " + batch);
                            }
                        }
                    } catch (IOException e) {
                        if (log.isErrorEnabled()) {
                            log.error(e.getLocalizedMessage());
                        }
                        throw new IllegalStateException("Finish (removing from queue) cannot be done due to IO exception " + e.getLocalizedMessage());
                    }
                    peekedBatch = null;
                    isWaitingToFinish = false;
                    checkPendingAndContinue();
                } else {
                    peekedBatch = null;
                }
            }
        });
    }

    private void checkPendingAndContinue() {
        initializeIfRequired();
        if (!queueFile.isEmpty()) {
            try {
                byte[] eldest = queueFile.peek();
                if (eldest != null) {
                    peekedBatch = serializationStrategy.deserializeBatch(eldest);
                    callPersistSuccess(peekedBatch);
                }
            } catch (IOException | DeserializeException e) {
                if (log.isErrorEnabled()) {
                    log.error(e.getLocalizedMessage());
                }
            }
        } else {
            callQueueEnd();
        }
    }

    private void callQueueEnd() {
        if (listener != null) {
            listener.onFinish();
        }
    }
}
