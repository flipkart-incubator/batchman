package com.flipkart.batching.listener;

import android.os.Handler;

import com.flipkart.batching.Batch;
import com.flipkart.batching.BatchingStrategy;
import com.flipkart.batching.Data;
import com.flipkart.batching.OnBatchReadyListener;
import com.flipkart.batching.persistence.SerializationStrategy;
import com.flipkart.batching.tape.FileObjectQueue;
import com.flipkart.batching.tape.InMemoryObjectQueue;
import com.flipkart.batching.tape.ObjectQueue;

import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;


/**
 * PersistedBatchReadyListener that implements {@link OnBatchReadyListener}.
 */

public class PersistedBatchReadyListener<E extends Data, T extends Batch<E>> implements OnBatchReadyListener<E, T> {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(PersistedBatchReadyListener.class);
    protected final Handler handler;
    protected final String filePath;
    private final Queue<T> cachedQueue;
    private PersistedBatchCallback<T> listener;
    private ObjectQueue<T> queueFile;
    private FileObjectQueue.Converter<T> converter;
    private boolean isWaitingToFinish;
    private T peekedBatch;

    public PersistedBatchReadyListener(String filePath, final SerializationStrategy<E, T> serializationStrategy, Handler handler, @Nullable PersistedBatchCallback<T> listener) {
        this.filePath = filePath;
        this.handler = handler;
        this.listener = listener;
        this.converter = new FileObjectQueue.Converter<T>() {
            @Override
            public T from(byte[] bytes) throws IOException {
                return serializationStrategy.deserializeBatch(bytes);
            }

            @Override
            public void toStream(T batch, OutputStream bytes) throws IOException {
                bytes.write(serializationStrategy.serializeBatch(batch));
            }
        };
        this.cachedQueue = (Queue<T>) new LinkedList();
    }

    public PersistedBatchCallback getListener() {
        return listener;
    }

    public void setListener(PersistedBatchCallback<T> listener) {
        this.listener = listener;
    }

    public ObjectQueue<T> getQueueFile() {
        return queueFile;
    }

    public void setQueueFile(ObjectQueue<T> queueFile) {
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
                    queueFile.add(batch);
                    cachedQueue.add(batch);
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

    protected int getSize() {
        return queueFile.size();
    }

    protected boolean remove(int num) {
        boolean result = false;
        try {
            int cachedSize = cachedQueue.size();
            int itemsNotCached = queueFile.size() - cachedSize;
            queueFile.remove(num);
            if (num > itemsNotCached) {
                int numItemsToBeRemoved = num - itemsNotCached;
                for (int i = 0; i < numItemsToBeRemoved; i++) {
                    cachedQueue.remove();
                }
            }
            result = true;
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getLocalizedMessage());
            }
        }
        return result;
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
                File file = new File(filePath);
                this.queueFile = new FileObjectQueue<>(file, converter);
                onInitialized();
            } catch (IOException e) {
                this.queueFile = new InMemoryObjectQueue<>();
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

    protected void onInitialized() {
    }

    public void finish(final T batch) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (queueFile.size() > 0) {
                    try {
                        if (peekedBatch != null) {
                            if (batch != null && batch.equals(peekedBatch)) {
                                boolean isCached = cachedQueue.size() == queueFile.size();
                                queueFile.remove();
                                if (isCached) {
                                    cachedQueue.remove();
                                }
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
        if (queueFile.size() > 0) {
            try {
                peekedBatch = (queueFile.size() == cachedQueue.size())
                        ? cachedQueue.peek()
                        : queueFile.peek();

                if (peekedBatch != null) {
                    callPersistSuccess(peekedBatch);
                }
            } catch (IOException e) {
                e.printStackTrace();
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
