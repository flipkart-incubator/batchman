package com.flipkart.batching;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import com.flipkart.data.Data;
import com.flipkart.persistence.PersistenceStrategy;
import com.flipkart.persistence.SerializationStrategy;

import java.util.Collections;

/**
 * Created by kushal.sharma on 07/02/16.
 */

public class BatchManager implements BatchController {

    private OnBatchReadyListener onBatchReadyListener;
    private BatchingStrategy batchingStrategy;
    private Handler handler;
    private SerializationStrategy serializationStrategy;


    protected BatchManager(Builder builder, Context context) {
        this.onBatchReadyListener = builder.getOnBatchReadyListener();
        this.batchingStrategy = builder.getBatchingStrategy();
        this.serializationStrategy = builder.getSerializationStrategy();
        this.handler = builder.getHandler();

        if (handler == null) {
            HandlerThread handlerThread = new HandlerThread("HandlerThread");
            handlerThread.start();
            this.handler = new Handler(handlerThread.getLooper());
        }

        initialize(this, context, onBatchReadyListener, handler);
    }

    @Override
    public void addToBatch(final Data data) {
        this.handler.post(new Runnable() {
            @Override
            public void run() {
                batchingStrategy.onDataPushed(Collections.singleton(data));
                batchingStrategy.flush(false);
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
     * This method takes {@link BatchController} and {@link Context} as parameters and initialize
     * the provided {@link PersistenceStrategy} and {@link BatchingStrategy}.
     *
     * @param batchController instance of batch controller
     * @param context         context
     */

    private void initialize(BatchController batchController, Context context, OnBatchReadyListener onBatchReadyListener, Handler handler) {
        batchingStrategy.onInitialized(batchController, context, onBatchReadyListener, handler);
    }


    public static class Builder {
        private OnBatchReadyListener onBatchReadyListener;
        private BatchingStrategy batchingStrategy;
        private Handler handler;
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
