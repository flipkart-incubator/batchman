/*
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2017 Flipkart Internet Pvt. Ltd.
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

package com.flipkart.batching.listener;

import android.os.Handler;
import androidx.annotation.Nullable;

import com.flipkart.batching.BatchingStrategy;
import com.flipkart.batching.OnBatchReadyListener;
import com.flipkart.batching.core.Batch;
import com.flipkart.batching.core.Data;
import com.flipkart.batching.core.SerializationStrategy;
import com.flipkart.batching.persistence.BatchObjectConverter;
import com.flipkart.batching.tape.InMemoryObjectQueue;
import com.flipkart.batching.tape.ObjectQueue;
import com.flipkart.batching.toolbox.LenientFileObjectQueue;
import com.flipkart.batching.toolbox.LenientQueueFile;
import com.flipkart.batching.toolbox.LogUtil;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * PersistedBatchReadyListener that implements {@link OnBatchReadyListener}.
 */
public class PersistedBatchReadyListener<E extends Data, T extends Batch<E>> implements OnBatchReadyListener<E, T>, LenientQueueFile.QueueFileErrorCallback {
    private static final int MAX_ITEMS_CACHED = 2000;
    private static final String TAG = "PersistedBatchReadyListener";
    final Handler handler;
    final String filePath;
    final Queue<T> cachedQueue;
    PersistedBatchCallback<T> listener;
    ObjectQueue<T> queueFile;
    BatchObjectConverter<E, T> converter;
    boolean isWaitingToFinish;
    T peekedBatch;

    public PersistedBatchReadyListener(String filePath, SerializationStrategy<E, T> serializationStrategy, Handler handler, @Nullable PersistedBatchCallback<T> listener) {
        this.filePath = filePath;
        this.handler = handler;
        this.listener = listener;
        this.converter = new BatchObjectConverter(serializationStrategy);
        this.cachedQueue = new LinkedList();
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
                    if (cachedQueue.size() == MAX_ITEMS_CACHED) {
                        cachedQueue.remove();
                    }
                    cachedQueue.add(batch);
                    checkPendingAndContinue();
                } catch (Exception e) {
                    LogUtil.log(TAG, e.getLocalizedMessage());
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
            LogUtil.log(TAG, e.getLocalizedMessage());
        }
        return result;
    }

    void callPersistFailure(T batch, Exception e) {
        if (listener != null) {
            listener.onPersistFailure(batch, e);
        }
    }

    private void callPersistSuccess(T batch) {
        if (listener != null) {
            listener.onPersistSuccess(batch);
        }

    }

    void initializeIfRequired() {
        if (!isInitialized()) {
            tryCreatingQueueFile();
            onInitialized();
        }
    }

    private void tryCreatingQueueFile() {
        try {
            File file = new File(filePath);
            this.queueFile = new LenientFileObjectQueue<>(file, converter, this);
        } catch (IOException e) {
            this.queueFile = new InMemoryObjectQueue<>();
            LogUtil.log(TAG, e.getLocalizedMessage());
        }
    }

    @Override
    public void onQueueFileOperationError(Throwable e) {
        LogUtil.log(TAG, "QueueFile {} is corrupt, gonna recreate it" + filePath);
        File file = new File(filePath);
        file.delete();
        tryCreatingQueueFile();
    }

    public void close() {
        if (queueFile != null) {
            try {
                queueFile.close();
            } catch (IOException e) {
                LogUtil.log(TAG, e.getLocalizedMessage());
            }
        }
    }

    protected void onInitialized() {
    }

    public void finish(final T batch) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                int queueSize = queueFile.size();
                if (queueSize > 0) {
                    try {
                        if (peekedBatch != null) {
                            if (batch != null && batch.equals(peekedBatch)) {
                                boolean isCached = cachedQueue.size() == queueSize;
                                queueFile.remove();
                                if (isCached) {
                                    cachedQueue.remove();
                                }
                            } else {
                                // We are currently seeing this very very rarely. We want to get more info here before we throw this exception
                                LogUtil.log(TAG, "Finish was called with a different batch, expected " + peekedBatch + " was " + batch);
                                // throw new IllegalStateException("Finish was called with a different batch, expected " + peekedBatch + " was " + batch);
                            }
                        }
                    } catch (IOException e) {
                        LogUtil.log(TAG, e.getLocalizedMessage());
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

    public void checkPendingAndContinue() {
        initializeIfRequired();
        if (queueFile.size() > 0) {
            try {
                if (!isWaitingToFinish) {
                    peekedBatch = (queueFile.size() == cachedQueue.size())
                            ? cachedQueue.peek()
                            : queueFile.peek();
                    if (peekedBatch != null) {
                        isWaitingToFinish = true;
                        callPersistSuccess(peekedBatch);
                    }
                }
            } catch (IOException e) {
                LogUtil.log(TAG, e.getLocalizedMessage());
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