package com.flipkart.batching;

import android.content.Context;
import android.os.Handler;

import com.flipkart.data.Data;
import com.flipkart.data.Tag;

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

public class TagBatchingStrategy implements BatchingStrategy {
    private Map<Tag, BatchingStrategy> batchingStrategyMap = new HashMap<>();
    private boolean initialized = false;

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void onDataPushed(Collection<Data> dataCollection) {
        for (Data data : dataCollection) {
            BatchingStrategy batchingStrategy = batchingStrategyMap.get(data.getTag());
            if (batchingStrategy != null) {
                batchingStrategy.onDataPushed(Collections.singleton(data));
            }
        }
    }

    @Override
    public void onInitialized(BatchController controller, Context context, final OnBatchReadyListener parentBatchReadyListener, Handler handler) {
        initialized = true;
        OnBatchReadyListener childBatchReadyListener = new OnBatchReadyListener() {
            @Override
            public void onReady(BatchingStrategy causingStrategy, BatchInfo batchInfo, Collection<Data> dataCollection) {
                Tag tag = getTagByStrategy(causingStrategy);
                parentBatchReadyListener.onReady(TagBatchingStrategy.this, new TagBatchInfo(tag, batchInfo), dataCollection); //this listener overrides the causing strategy
            }
        };

        for (Tag tag : batchingStrategyMap.keySet()) {
            batchingStrategyMap.get(tag).onInitialized(controller, context, childBatchReadyListener, handler);
        }
    }

    public Tag getTagByStrategy(BatchingStrategy causingStrategy) {
        for (Map.Entry<Tag, BatchingStrategy> entry : batchingStrategyMap.entrySet()) {
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
        for (Tag tag : batchingStrategyMap.keySet())
            batchingStrategyMap.get(tag).flush(forced);
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

    public void addTagStrategy(Tag tag, BatchingStrategy strategy) {
        batchingStrategyMap.put(tag, strategy);
    }


    public static class TagBatchInfo implements BatchInfo {
        private Tag tag;
        private BatchInfo childBatchInfo;

        public TagBatchInfo(Tag tag, BatchInfo childBatchInfo) {
            this.tag = tag;
            this.childBatchInfo = childBatchInfo;
        }

        public BatchInfo getChildBatchInfo() {
            return childBatchInfo;
        }

        public Tag getTag() {
            return tag;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof TagBatchInfo) {
                return ((TagBatchInfo) o).getTag() == tag && ((TagBatchInfo) o).getChildBatchInfo() == childBatchInfo;
            }
            return super.equals(o);
        }
    }
}
