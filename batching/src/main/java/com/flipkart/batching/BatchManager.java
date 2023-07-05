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

package com.flipkart.batching;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import com.flipkart.batching.core.Batch;
import com.flipkart.batching.core.Data;
import com.flipkart.batching.core.SerializationStrategy;
import com.flipkart.batching.toolbox.LogUtil;

import java.util.Collection;

/**
 * BatchManager that implements {@link BatchController} interface. BatchManager uses builder pattern
 * to initialize an implementation of {@link BatchingStrategy}, {@link SerializationStrategy},
 * {@link Handler} and {@link OnBatchReadyListener}.
 * <p/>
 * BatchManager sends the initialized {@link BatchController}, {@link Context},
 * {@link OnBatchReadyListener} and {@link Handler} to the initialized object of BatchingStrategy.
 * <p/>
 * {@link #addToBatch(Collection)} tells the BatchingStrategy about the input dataCollection.
 * <p/>
 * {@link #getSerializationStrategy()} returns the SerializationStrategy provided while building
 * the BatchManger instance.
 * <p/>
 * {@link #getHandler()} returns the Handler provided while building the BatchManger instance.
 *
 * @see BatchController
 */

public class BatchManager<E extends Data, T extends Batch<E>> implements BatchController<E, T> {
    Handler handler;
    BatchingStrategy<E, T> batchingStrategy;
    SerializationStrategy<E, T> serializationStrategy;

    protected BatchManager(Builder builder, final Context context) {
        final OnBatchReadyListener onBatchReadyListener = builder.getOnBatchReadyListener();
        this.batchingStrategy = builder.getBatchingStrategy();
        this.serializationStrategy = builder.getSerializationStrategy();
        this.handler = builder.getHandler();
        if (handler == null) {
            HandlerThread handlerThread = new HandlerThread("HandlerThread");
            handlerThread.start();
            this.handler = new Handler(handlerThread.getLooper());
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                serializationStrategy.build();
                initialize(BatchManager.this, context, onBatchReadyListener, handler);
            }
        });
    }

    @Override
    public void addToBatch(final Collection<E> dataCollection) {
        addToBatch(dataCollection, false);
    }

    @Override
    public void addToBatch(final Collection<E> dataCollection, final boolean forced) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                assignEventIds(dataCollection);
                if (batchingStrategy.isInitialized()) {
                    batchingStrategy.onDataPushed(dataCollection);
                    batchingStrategy.flush(forced);
                } else {
                    throw new IllegalAccessError("BatchingStrategy is not initialized");
                }
            }
        });
    }

    void assignEventIds(Collection<E> dataCollection) {
        int i = 0;
        for (E data : dataCollection) {
            i++;
            data.setEventId(System.currentTimeMillis() + System.nanoTime() + i);
        }
    }

    @Override
    public Handler getHandler() {
        return this.handler;
    }

    @Override
    public SerializationStrategy<E, T> getSerializationStrategy() {
        return this.serializationStrategy;
    }

    @Override
    public void flush(final boolean forced) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                batchingStrategy.flush(forced);
            }
        });
    }

    /**
     * This method takes {@link BatchController}, {@link Context}, {@link Handler} and
     * {@link OnBatchReadyListener} as parameters, after building the BatchManager and passes
     * it to the instance of provided {@link BatchingStrategy}.
     *
     * @param batchController      instance of {@link BatchController}
     * @param context              context
     * @param onBatchReadyListener instance of {@link OnBatchReadyListener}
     * @param handler              instance of {@link Handler}
     */

    public void initialize(BatchController<E, T> batchController, Context context,
                           OnBatchReadyListener<E, T> onBatchReadyListener, Handler handler) {
        batchingStrategy.onInitialized(context, onBatchReadyListener, handler);
    }

    public static class Builder<E extends Data, T extends Batch<E>> {
        private Handler handler;
        private BatchingStrategy batchingStrategy;
        private OnBatchReadyListener onBatchReadyListener;
        private SerializationStrategy serializationStrategy;

        public SerializationStrategy getSerializationStrategy() {
            return serializationStrategy;
        }

        public Builder setSerializationStrategy(SerializationStrategy serializationStrategy) {
            if (serializationStrategy != null) {
                this.serializationStrategy = serializationStrategy;
            } else {
                throw new IllegalArgumentException("Serialization Strategy cannot be null");
            }
            return this;
        }

        public OnBatchReadyListener getOnBatchReadyListener() {
            return onBatchReadyListener;
        }

        public Builder setOnBatchReadyListener(OnBatchReadyListener onBatchReadyListener) {
            if (onBatchReadyListener != null) {
                this.onBatchReadyListener = onBatchReadyListener;
            } else {
                throw new IllegalArgumentException("OnBatchReadyListener not specified");
            }
            return this;
        }

        public BatchingStrategy getBatchingStrategy() {
            return batchingStrategy;
        }

        public Builder setBatchingStrategy(BatchingStrategy batchingStrategy) {
            if (batchingStrategy != null) {
                this.batchingStrategy = batchingStrategy;
            } else {
                throw new IllegalArgumentException("BatchingStrategy cannot be null");
            }
            return this;
        }

        public Handler getHandler() {
            return handler;
        }

        public Builder setHandler(Handler handler) {
            this.handler = handler;
            return this;
        }

        public Builder enableLogging() {
            LogUtil.isLoggingEnabled = true;
            return this;
        }

        public BatchManager<E, T> build(Context context) {
            return new BatchManager<>(this, context);
        }

    }
}
