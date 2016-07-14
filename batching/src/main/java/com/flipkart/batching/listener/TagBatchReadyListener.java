/*
 * Copyright 2016 Flipkart Internet Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flipkart.batching.listener;

import com.flipkart.batching.BatchingStrategy;
import com.flipkart.batching.OnBatchReadyListener;
import com.flipkart.batching.data.Tag;
import com.flipkart.batching.data.TagData;
import com.flipkart.batching.strategy.TagBatchingStrategy;

import java.util.HashMap;
import java.util.Map;

/**
 * TagBatchReadyListener that implements {@link OnBatchReadyListener}.
 */
public class TagBatchReadyListener<E extends TagData> implements OnBatchReadyListener<E, TagBatchingStrategy.TagBatch<E>> {
    private Map<Tag, OnBatchReadyListener<E, TagBatchingStrategy.TagBatch<E>>> tagOnBatchReadyListenerMap;

    public TagBatchReadyListener() {
        tagOnBatchReadyListenerMap = new HashMap<>();
    }

    public Map<Tag, OnBatchReadyListener<E, TagBatchingStrategy.TagBatch<E>>> getTagOnBatchReadyListenerMap() {
        return tagOnBatchReadyListenerMap;
    }

    public void addListenerForTag(Tag tag, OnBatchReadyListener<E, TagBatchingStrategy.TagBatch<E>> listener) {
        tagOnBatchReadyListenerMap.put(tag, listener);
    }

    private OnBatchReadyListener<E, TagBatchingStrategy.TagBatch<E>> getListenerByTag(Tag tag) {
        return tagOnBatchReadyListenerMap.get(tag);
    }

    @Override
    public void onReady(BatchingStrategy<E, TagBatchingStrategy.TagBatch<E>> causingStrategy, TagBatchingStrategy.TagBatch<E> batch) {
        getListenerByTag(batch.getTag()).onReady(causingStrategy, batch);
    }
}
