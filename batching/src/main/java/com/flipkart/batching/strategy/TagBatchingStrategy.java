/*
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2016 Flipkart Internet Pvt. Ltd.
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

package com.flipkart.batching.strategy;

import android.content.Context;
import android.os.Handler;

import com.flipkart.batching.BatchingStrategy;
import com.flipkart.batching.OnBatchReadyListener;
import com.flipkart.batching_core.Batch;
import com.flipkart.batching_core.Data;
import com.flipkart.batching_core.batch.TagBatch;
import com.flipkart.batching_core.data.Tag;
import com.flipkart.batching_core.data.TagData;

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
public class TagBatchingStrategy<E extends TagData> implements BatchingStrategy<E, TagBatch<E>> {
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
     * <p>
     * Whenever new data is pushed, tag is checked and data is pushed to the specified
     * batching strategy.
     *
     * @param tag      {@link Tag} type tag
     * @param strategy {@link BatchingStrategy} type strategy
     */
    public void addTagStrategy(Tag tag, BatchingStrategy<E, Batch<E>> strategy) {
        batchingStrategyMap.put(tag, strategy);
    }
}
