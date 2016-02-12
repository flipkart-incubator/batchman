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
 * Created by kushal.sharma on 09/02/16.
 */

public class TagBatchingStrategy implements BatchingStrategy {
    private Map<Tag, BatchingStrategy> batchingStrategyMap = new HashMap<>();

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
    public void onInitialized(BatchController controller, Context context, OnBatchReadyListener onBatchReadyListener, Handler handler) {
        for (Tag tag : batchingStrategyMap.keySet()) {
            batchingStrategyMap.get(tag).onInitialized(controller, context, onBatchReadyListener, handler);
        }
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
}