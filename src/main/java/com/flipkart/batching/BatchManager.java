package com.flipkart.batching;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import com.flipkart.data.Data;
import com.flipkart.persistence.SerializationStrategy;

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

public class BatchManager implements BatchController {
    private Handler handler;
    private BatchingStrategy batchingStrategy;
    private SerializationStrategy serializationStrategy;

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
                initialize(BatchManager.this, context, onBatchReadyListener, handler);
            }
        });
    }

    @Override
    public void addToBatch(final Collection<Data> dataCollection) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (batchingStrategy.isInitialized()) {
                    batchingStrategy.onDataPushed(dataCollection);
                    batchingStrategy.flush(false);
                } else {
                    try {
                        throw new Exception("Batching Strategy not initialized");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public Handler getHandler() {
        return this.handler;
    }

    @Override
    public SerializationStrategy getSerializationStrategy() {
        return this.serializationStrategy;
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
    private void initialize(BatchController batchController, Context context,
                            OnBatchReadyListener onBatchReadyListener, Handler handler) {
        batchingStrategy.onInitialized(batchController, context, onBatchReadyListener, handler);
    }

    public static class Builder {
        private Handler handler;
        private BatchingStrategy batchingStrategy;
        private OnBatchReadyListener onBatchReadyListener;
        private SerializationStrategy serializationStrategy;

        public SerializationStrategy getSerializationStrategy() {
            return serializationStrategy;
        }

        public Builder setSerializationStrategy(SerializationStrategy serializationStrategy) {
            this.serializationStrategy = serializationStrategy;
            return this;
        }

        public OnBatchReadyListener getOnBatchReadyListener() {
            return onBatchReadyListener;
        }

        public Builder setOnBatchReadyListener(OnBatchReadyListener onBatchReadyListener) {
            this.onBatchReadyListener = onBatchReadyListener;
            return this;
        }

        public BatchingStrategy getBatchingStrategy() {
            return batchingStrategy;
        }

        public Builder setBatchingStrategy(BatchingStrategy batchingStrategy) {
            this.batchingStrategy = batchingStrategy;
            return this;
        }

        public Handler getHandler() {
            return handler;
        }

        public Builder setHandler(Handler handler) {
            this.handler = handler;
            return this;
        }

        public BatchManager build(Context context) {
            return new BatchManager(this, context);
        }
    }
}
