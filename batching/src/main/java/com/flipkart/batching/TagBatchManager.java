package com.flipkart.batching;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import com.flipkart.batching.data.EventData;
import com.flipkart.batching.data.Tag;
import com.flipkart.batching.data.TagData;
import com.flipkart.batching.listener.NetworkPersistedBatchReadyListener;
import com.flipkart.batching.listener.TagBatchReadyListener;
import com.flipkart.batching.persistence.SerializationStrategy;
import com.flipkart.batching.strategy.ComboBatchingStrategy;
import com.flipkart.batching.strategy.SizeBatchingStrategy;
import com.flipkart.batching.strategy.TagBatchingStrategy;
import com.flipkart.batching.strategy.TimeBatchingStrategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by kushal.sharma on 24/03/16.
 */

public class TagBatchManager<E extends Data, T extends Batch<E>> implements BatchController<E, T> {
    private Handler handler;
    private SerializationStrategy<E, T> serializationStrategy;
    private TagBatchingStrategy<TagData> tagBatchingStrategy;
    private TagBatchReadyListener<TagData> tagBatchReadyListener;
    private ArrayList<TagInfo> tagParametersList = new ArrayList<>();

    protected TagBatchManager(Builder builder, final Context context) {
        tagBatchingStrategy = new TagBatchingStrategy<>();
        tagBatchReadyListener = new TagBatchReadyListener<>();

        tagParametersList = builder.getTagInfoList();

        for (int i = 0; i < tagParametersList.size(); i++) {
            tagBatchReadyListener.addListenerForTag(tagParametersList.get(i).tag, tagParametersList.get(i).tagBatchReadyListener);
            tagBatchingStrategy.addTagStrategy(tagParametersList.get(i).tag, tagParametersList.get(i).tagComboBatchingStrategy);
        }

        this.serializationStrategy = builder.getSerializationStrategy();

        this.handler = builder.getHandler();
        if (handler == null) {
            HandlerThread handlerThread = new HandlerThread("HandlerThread");
            handlerThread.start();
            this.handler = new Handler(handlerThread.getLooper());
        }

        registerBuiltInTypes(serializationStrategy);
        registerSuppliedTypes(builder, serializationStrategy);

        handler.post(new Runnable() {
            @Override
            public void run() {
                serializationStrategy.build();
                initialize(TagBatchManager.this, context, tagBatchReadyListener, handler);
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

    private void initialize(TagBatchManager<E, T> tagBatchManager, Context context, OnBatchReadyListener onBatchReadyListener, Handler handler) {
        tagBatchingStrategy.onInitialized(context, onBatchReadyListener, handler);
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
                assignEventIds(dataCollection);
                if (tagBatchingStrategy.isInitialized()) {
                    tagBatchingStrategy.onDataPushed((Collection<TagData>) dataCollection);
                    tagBatchingStrategy.flush(false);
                } else {
                    throw new IllegalAccessError("BatchingStrategy is not initialized");
                }
            }
        });
    }

    private void assignEventIds(Collection<E> dataCollection) {
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
                tagBatchingStrategy.flush(forced);
            }
        });
    }

    public static class Builder<E extends Data, T extends Batch<E>> {
        private Handler handler;
        private SerializationStrategy serializationStrategy;
        private Set<Class<E>> dataTypes = new HashSet<>();
        private Set<Class<T>> batchInfoTypes = new HashSet<>();
        private ArrayList<TagInfo> tagInfoList = new ArrayList<>();

        public ArrayList<TagInfo> getTagInfoList() {
            return tagInfoList;
        }

        public Builder addTag(Tag tag, BatchingStrategy batchingStrategy,
                              NetworkPersistedBatchReadyListener onBatchReadyListener) {
            this.tagInfoList.add(new TagInfo(tag, batchingStrategy, onBatchReadyListener));
            return this;
        }

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

        public TagBatchManager<E, T> build(Context context) {
            return new TagBatchManager<>(this, context);
        }
    }

    public static class TagInfo {
        public Tag tag;
        public BatchingStrategy tagComboBatchingStrategy;
        public OnBatchReadyListener tagBatchReadyListener;

        public TagInfo(Tag tag, BatchingStrategy batchingStrategy, NetworkPersistedBatchReadyListener onBatchReadyListener) {
            this.tag = tag;
            this.tagComboBatchingStrategy = batchingStrategy;
            this.tagBatchReadyListener = onBatchReadyListener;
        }
    }
}
