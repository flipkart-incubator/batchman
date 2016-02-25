package com.flipkart.batching.listener;

import com.flipkart.batching.Batch;
import com.flipkart.batching.BatchingStrategy;
import com.flipkart.batching.OnBatchReadyListener;
import com.flipkart.batching.data.Tag;
import com.flipkart.batching.data.TagData;
import com.flipkart.batching.strategy.TagBatchingStrategy;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kushal.sharma on 13/02/16.
 * Todo Document
 */

public class TagBatchReadyListener<E extends TagData> implements OnBatchReadyListener<E, TagBatchingStrategy.TagBatch<E, Batch<E>>> {
    private Map<Tag, OnBatchReadyListener<E, TagBatchingStrategy.TagBatch<E, Batch<E>>>> tagOnBatchReadyListenerMap;

    public TagBatchReadyListener() {
        tagOnBatchReadyListenerMap = new HashMap<>();
    }

    public Map<Tag, OnBatchReadyListener<E, TagBatchingStrategy.TagBatch<E, Batch<E>>>> getTagOnBatchReadyListenerMap() {
        return tagOnBatchReadyListenerMap;
    }

    public void addListenerForTag(Tag tag, OnBatchReadyListener<E, TagBatchingStrategy.TagBatch<E, Batch<E>>> listener) {
        tagOnBatchReadyListenerMap.put(tag, listener);
    }

    private OnBatchReadyListener<E, TagBatchingStrategy.TagBatch<E, Batch<E>>> getListenerByTag(Tag tag) {
        return tagOnBatchReadyListenerMap.get(tag);
    }

    @Override
    public void onReady(BatchingStrategy<E, TagBatchingStrategy.TagBatch<E, Batch<E>>> causingStrategy, TagBatchingStrategy.TagBatch<E, Batch<E>> batch) {
        getListenerByTag(batch.getTag()).onReady(causingStrategy, batch);
    }
}
