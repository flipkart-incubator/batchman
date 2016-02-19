package com.flipkart.batching;

import com.flipkart.data.Data;
import com.flipkart.data.Tag;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kushal.sharma on 13/02/16.
 * Todo Document
 */

public class TagPersistedBatchReadyListener implements OnBatchReadyListener {
    Map<Tag, OnBatchReadyListener> tagOnBatchReadyListenerMap;

    public TagPersistedBatchReadyListener() {
        tagOnBatchReadyListenerMap = new HashMap<>();
    }

    public void addListenerForTag(Tag tag, OnBatchReadyListener listener) {
        tagOnBatchReadyListenerMap.put(tag, listener);
    }

    @Override
    public void onReady(BatchingStrategy causingStrategy, BatchInfo batchInfo, Collection<Data> dataCollection) {
        if (batchInfo instanceof TagBatchingStrategy.TagBatchInfo) {
            Tag tag = (((TagBatchingStrategy.TagBatchInfo) batchInfo)).getTag();
            tagOnBatchReadyListenerMap.get(tag).onReady(causingStrategy, batchInfo, dataCollection);
        } else {
            throw new IllegalStateException("Use TagPersistedBatchReadyListener for tagBasedStrategy");
        }
    }

    private OnBatchReadyListener getListenerByTag(Tag tag) {
        return tagOnBatchReadyListenerMap.get(tag);
    }
}
