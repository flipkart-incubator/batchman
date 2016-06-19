package com.flipkart.batching.strategy;

import android.content.Context;
import android.os.Handler;

import com.flipkart.batching.Batch;
import com.flipkart.batching.BatchingStrategy;
import com.flipkart.batching.Data;
import com.flipkart.batching.OnBatchReadyListener;
import com.flipkart.batching.data.Tag;
import com.flipkart.batching.data.TagData;
import com.google.gson.annotations.SerializedName;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * TagBatchingStrategy is an implementation of {@link BatchingStrategy}.
 * {@link #addTagStrategy(Tag, BatchingStrategy)} map the provided tag with the desired
 * {@link BatchingStrategy}. Whenever {@link Data} objects are pushed, this strategy will
 * check the tag of added object and pass it to the defined batching strategy.
 *
 * @see BatchingStrategy
 * @see BaseBatchingStrategy
 * @see SizeBatchingStrategy
 * @see TimeBatchingStrategy
 * @see TagBatchingStrategy
 */
public class TagBatchingStrategy<E extends TagData> implements BatchingStrategy<E, TagBatchingStrategy.TagBatch<E>> {
    private Map<Tag, BatchingStrategy<E, Batch<E>>> batchingStrategyMap = new HashMap<>();
    private boolean initialized = false;

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void onDataPushed(Collection<E> dataCollection) {
        for (TagData data : dataCollection) {
            BatchingStrategy batchingStrategy = batchingStrategyMap.get(data.getTag());
            if (batchingStrategy != null) {
                batchingStrategy.onDataPushed(Collections.singleton(data));
            }
        }
    }

    @Override
    public void onInitialized(Context context, final OnBatchReadyListener<E, TagBatch<E>> parentBatchReadyListener, Handler handler) {
        initialized = true;
        OnBatchReadyListener<E, Batch<E>> childBatchReadyListener = new OnBatchReadyListener<E, Batch<E>>() {
            @Override
            public void onReady(BatchingStrategy<E, Batch<E>> causingStrategy, Batch<E> batch) {
                Tag tag = getTagByStrategy(causingStrategy);
                parentBatchReadyListener.onReady(TagBatchingStrategy.this, new TagBatch(tag, batch)); //this listener overrides the causing strategy
            }
        };

        for (Tag tag : batchingStrategyMap.keySet()) {
            batchingStrategyMap.get(tag).onInitialized(context, childBatchReadyListener, handler);
        }
    }

    public Tag getTagByStrategy(BatchingStrategy<E, Batch<E>> causingStrategy) {
        for (Map.Entry<Tag, BatchingStrategy<E, Batch<E>>> entry : batchingStrategyMap.entrySet()) {
            BatchingStrategy batchingStrategy = entry.getValue();
            Tag tag = entry.getKey();
            if (batchingStrategy == causingStrategy) {
                return tag;
            }
        }
        return null;
    }

    @Override
    public void flush(boolean forced) {
        for (Tag tag : batchingStrategyMap.keySet()) {
            batchingStrategyMap.get(tag).flush(forced);
        }
    }

    /**
     * This method takes {@link Tag} and {@link BatchingStrategy} as parameters and
     * adds the data to batchingStrategyMap.
     * <p/>
     * Whenever new data is pushed, tag is checked and data is pushed to the specified
     * batching strategy.
     *
     * @param tag      {@link Tag} type tag
     * @param strategy {@link BatchingStrategy} type strategy
     */
    public void addTagStrategy(Tag tag, BatchingStrategy<E, Batch<E>> strategy) {
        batchingStrategyMap.put(tag, strategy);
    }


    public static class TagBatch<T extends TagData> extends Batch<T> {
        @SerializedName("tag")
        private Tag tag;

        public TagBatch(Tag tag, Batch<T> batch) {
            super(batch.getDataCollection());
            this.tag = tag;
        }

        public Tag getTag() {
            return tag;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof TagBatch) {
                return ((TagBatch) o).getTag().equals(tag) && super.equals(o);
            }
            return super.equals(o);
        }

        @Override
        public int hashCode() {
            return getTag() == null ? 0 : getTag().hashCode();
        }
    }
}
