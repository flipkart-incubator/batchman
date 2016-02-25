package com.flipkart.batching.strategy;

import android.content.Context;
import android.os.Handler;

import com.flipkart.batching.Batch;
import com.flipkart.batching.BatchController;
import com.flipkart.batching.BatchingStrategy;
import com.flipkart.batching.OnBatchReadyListener;
import com.flipkart.batching.Data;
import com.flipkart.batching.data.Tag;
import com.flipkart.batching.data.TagData;

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

public class TagBatchingStrategy<E extends TagData, C extends Batch<E>> implements BatchingStrategy<E, TagBatchingStrategy.TagBatch<E,C>> {
    private Map<Tag, BatchingStrategy<E, C>> batchingStrategyMap = new HashMap<>();
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
    public void onInitialized(Context context, final OnBatchReadyListener<E, TagBatch<E, C>> parentBatchReadyListener, Handler handler) {
        initialized = true;
        OnBatchReadyListener<E, C> childBatchReadyListener = new OnBatchReadyListener<E, C>() {
            @Override
            public void onReady(BatchingStrategy<E, C> causingStrategy, C batch) {
                Tag tag = getTagByStrategy(causingStrategy);
                parentBatchReadyListener.onReady(TagBatchingStrategy.this, new TagBatch(tag, batch)); //this listener overrides the causing strategy
            }
        };

        for (Tag tag : batchingStrategyMap.keySet()) {
            batchingStrategyMap.get(tag).onInitialized(context, childBatchReadyListener, handler);
        }
    }

    public Tag getTagByStrategy(BatchingStrategy<E, C> causingStrategy) {
        for (Map.Entry<Tag, BatchingStrategy<E, C>> entry : batchingStrategyMap.entrySet()) {
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

    public void addTagStrategy(Tag tag, BatchingStrategy<E, C> strategy) {
        batchingStrategyMap.put(tag, strategy);
    }


    public static class TagBatch<T extends TagData, C extends Batch<T>> extends Batch<T> {
        private Tag tag;
        private C batch;

        public TagBatch(Tag tag,C batch) {
            super(null);
            this.tag = tag;
            this.batch = batch;
        }

        public Tag getTag() {
            return tag;
        }

        @Override
        public Collection<T> getDataCollection() {
            return batch.getDataCollection();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof TagBatch) {
                return ((TagBatch) o).getTag() == tag;
            }
            return super.equals(o);
        }
    }
}
