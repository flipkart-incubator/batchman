/*
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2017 Flipkart Internet Pvt. Ltd.
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

import com.flipkart.Utils;
import com.flipkart.batching.BatchingStrategy;
import com.flipkart.batching.BuildConfig;
import com.flipkart.batching.OnBatchReadyListener;
import com.flipkart.batching.core.batch.SizeBatch;
import com.flipkart.batching.core.batch.TagBatch;
import com.flipkart.batching.core.data.Tag;
import com.flipkart.batching.core.data.TagData;
import com.flipkart.batching.strategy.TagBatchingStrategy;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test for {@link TagBatchReadyListener}
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class TagBatchReadyTest {


    /**
     * Test to add tag listeners.
     */
    @Test
    public void testAddTagListener() {
        TagBatchReadyListener<TagData> tagBatchReadyListener;
        Tag AD = new Tag("ADS");
        Tag BUSINESS = new Tag("BUSINESS");
        Tag DEBUG = new Tag("DEBUG");

        OnBatchReadyListener<TagData, TagBatch<TagData>> onBatchReadyListener = mock(OnBatchReadyListener.class);
        tagBatchReadyListener = new TagBatchReadyListener<>();
        tagBatchReadyListener.addListenerForTag(AD, onBatchReadyListener);
        tagBatchReadyListener.addListenerForTag(DEBUG, onBatchReadyListener);
        tagBatchReadyListener.addListenerForTag(BUSINESS, onBatchReadyListener);

        //assert that the tagMaps are not null
        Assert.assertTrue(!tagBatchReadyListener.getTagOnBatchReadyListenerMap().isEmpty());
    }


    /**
     * Test to verify {@link TagBatchReadyListener#onReady(BatchingStrategy, TagBatch)}
     */
    @Test
    public void testOnReady() {
        TagBatchReadyListener<TagData> tagBatchReadyListener;
        Tag AD = new Tag("ADS");
        Tag BUSINESS = new Tag("BUSINESS");
        Tag DEBUG = new Tag("DEBUG");

        OnBatchReadyListener<TagData, TagBatch<TagData>> onBatchReadyListener = mock(OnBatchReadyListener.class);
        tagBatchReadyListener = new TagBatchReadyListener<>();
        tagBatchReadyListener.addListenerForTag(AD, onBatchReadyListener);
        tagBatchReadyListener.addListenerForTag(DEBUG, onBatchReadyListener);
        tagBatchReadyListener.addListenerForTag(BUSINESS, onBatchReadyListener);

        tagBatchReadyListener.onReady(new TagBatchingStrategy<>(), new TagBatch<>(new Tag("ADS"), new SizeBatch(Utils.fakeCollection(2), 4)));
        //verify that it gets called once , when tagBatchReadyListener's onReady gets called
        verify(onBatchReadyListener, times(1)).onReady(any(BatchingStrategy.class), any(TagBatch.class));
    }
}