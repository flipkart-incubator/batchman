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
    int mode;

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

    void trimQueue() {
        int oldSize = getSize();
        if (oldSize >= maxQueueSize && remove(trimSize)) {
            callTrimListener(oldSize, getSize());
        }
    }

    private void callTrimListener(int oldSize, int newSize) {
        if (trimListener != null) {
            trimListener.onTrimmed(oldSize, newSize);
        }
    }
}
