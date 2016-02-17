package com.flipkart.batching;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import com.flipkart.data.Data;
import com.flipkart.data.EventData;
import com.flipkart.persistence.SerializationStrategy;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
        registerBuiltInTypes(serializationStrategy);
        registerSuppliedTypes(builder, serializationStrategy);
        serializationStrategy.build();
    }

    public static void registerBuiltInTypes(SerializationStrategy serializationStrategy) {
        serializationStrategy.registerDataType(EventData.class);
        serializationStrategy.registerBatchInfoType(SizeBatchingStrategy.SizeBatchInfo.class);
        serializationStrategy.registerBatchInfoType(TimeBatchingStrategy.TimeBatchInfo.class);
        serializationStrategy.registerBatchInfoType(TagBatchingStrategy.TagBatchInfo.class);
    }

    private void registerSuppliedTypes(Builder builder, SerializationStrategy serializationStrategy) {
        for (Class<? extends Data> dataType : builder.dataTypes) {
            serializationStrategy.registerDataType(dataType);
        }
        for (Class<? extends BatchInfo> batchInfoType : builder.batchInfoTypes) {
            serializationStrategy.registerBatchInfoType(batchInfoType);
        }
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
                    throw new IllegalAccessError("BatchingStrategy is not initialized");
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
        private Set<Class<? extends Data>> dataTypes = new HashSet<>();
        private Set<Class<? extends BatchInfo>> batchInfoTypes = new HashSet<>();

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

        public Builder registerDataType(Class<? extends Data> subClass) {
            dataTypes.add(subClass);
            return this;
        }

        public Builder registerBatchInfoType(Class<? extends BatchInfo> subClass) {
            batchInfoTypes.add(subClass);
            return this;
        }

        public BatchManager build(Context context) {
            return new BatchManager(this, context);
        }
    }
}
