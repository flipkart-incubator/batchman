package com.flipkart.batching;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import com.flipkart.batching.data.EventData;
import com.flipkart.batching.data.TagData;
import com.flipkart.batching.persistence.SerializationStrategy;
import com.flipkart.batching.strategy.ComboBatchingStrategy;
import com.flipkart.batching.strategy.SizeBatchingStrategy;
import com.flipkart.batching.strategy.TagBatchingStrategy;
import com.flipkart.batching.strategy.TimeBatchingStrategy;

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

public class BatchManager<E extends Data, T extends Batch<E>> implements BatchController<E, T> {
    private Handler handler;
    private BatchingStrategy<E, T> batchingStrategy;
    private SerializationStrategy<E, T> serializationStrategy;

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

        registerBuiltInTypes(serializationStrategy);
        registerSuppliedTypes(builder, serializationStrategy);
        serializationStrategy.build();
        handler.post(new Runnable() {
            @Override
            public void run() {
                initialize(BatchManager.this, context, onBatchReadyListener, handler);
            }
        });
    }

    public static void registerBuiltInTypes(SerializationStrategy serializationStrategy) {
        serializationStrategy.registerDataType(TagData.class);
        serializationStrategy.registerBatch(Batch.class);
        serializationStrategy.registerDataType(EventData.class);
        serializationStrategy.registerBatch(SizeBatchingStrategy.SizeBatch.class);
        serializationStrategy.registerBatch(TimeBatchingStrategy.TimeBatch.class);
        serializationStrategy.registerBatch(TagBatchingStrategy.TagBatch.class);
        serializationStrategy.registerBatch(ComboBatchingStrategy.ComboBatch.class);
    }

    private void registerSuppliedTypes(Builder<E, T> builder, SerializationStrategy serializationStrategy) {
        for (Class<E> dataType : builder.dataTypes) {
            serializationStrategy.registerDataType(dataType);
        }
        for (Class<T> batchInfoType : builder.batchInfoTypes) {
            serializationStrategy.registerBatch(batchInfoType);
        }
    }

    @Override
    public void addToBatch(final Collection<E> dataCollection) {
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
    public SerializationStrategy<E, T> getSerializationStrategy() {
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
    private void initialize(BatchController<E, T> batchController, Context context,
                            OnBatchReadyListener<E, T> onBatchReadyListener, Handler handler) {
        batchingStrategy.onInitialized(context, onBatchReadyListener, handler);
    }

    public static class Builder<E extends Data, T extends Batch<E>> {
        private Handler handler;
        private BatchingStrategy batchingStrategy;
        private OnBatchReadyListener onBatchReadyListener;
        private SerializationStrategy serializationStrategy;
        private Set<Class<E>> dataTypes = new HashSet<>();
        private Set<Class<T>> batchInfoTypes = new HashSet<>();

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

        public Builder registerDataType(Class<E> subClass) {
            dataTypes.add(subClass);
            return this;
        }

        public Builder registerBatchInfoType(Class<T> subClass) {
            batchInfoTypes.add(subClass);
            return this;
        }

        public BatchManager<E, T> build(Context context) {
            return new BatchManager<>(this, context);
        }
    }
}
