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

package com.flipkart.batching.listener;

import com.flipkart.batching.BatchingStrategy;
import com.flipkart.batching.OnBatchReadyListener;
import com.flipkart.batchingcore.batch.TagBatch;
import com.flipkart.batchingcore.data.Tag;
import com.flipkart.batchingcore.data.TagData;

import java.util.HashMap;
import java.util.Map;

/**
 * TagBatchReadyListener that implements {@link OnBatchReadyListener}.
 */
public class TagBatchReadyListener<E extends TagData> implements OnBatchReadyListener<E, TagBatch<E>> {
    private Map<Tag, OnBatchReadyListener<E, TagBatch<E>>> tagOnBatchReadyListenerMap;

    public TagBatchReadyListener() {
        tagOnBatchReadyListenerMap = new HashMap<>();
    }

    public Map<Tag, OnBatchReadyListener<E, TagBatch<E>>> getTagOnBatchReadyListenerMap() {
        return tagOnBatchReadyListenerMap;
    }

    public void addListenerForTag(Tag tag, OnBatchReadyListener<E, TagBatch<E>> listener) {
        tagOnBatchReadyListenerMap.put(tag, listener);
    }

    private OnBatchReadyListener<E, TagBatch<E>> getListenerByTag(Tag tag) {
        return tagOnBatchReadyListenerMap.get(tag);
    }

    @Override
    public void onReady(BatchingStrategy<E, TagBatch<E>> causingStrategy, TagBatch<E> batch) {
        getListenerByTag(batch.getTag()).onReady(causingStrategy, batch);
    }
}