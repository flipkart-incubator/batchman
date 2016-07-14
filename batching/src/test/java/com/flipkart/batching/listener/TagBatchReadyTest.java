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

import com.flipkart.Utils;
import com.flipkart.batching.BatchingStrategy;
import com.flipkart.batching.BuildConfig;
import com.flipkart.batching.OnBatchReadyListener;
import com.flipkart.batching.data.Tag;
import com.flipkart.batching.data.TagData;
import com.flipkart.batching.strategy.SizeBatchingStrategy;
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

        OnBatchReadyListener<TagData, TagBatchingStrategy.TagBatch<TagData>> onBatchReadyListener = mock(OnBatchReadyListener.class);
        tagBatchReadyListener = new TagBatchReadyListener<>();
        tagBatchReadyListener.addListenerForTag(AD, onBatchReadyListener);
        tagBatchReadyListener.addListenerForTag(DEBUG, onBatchReadyListener);
        tagBatchReadyListener.addListenerForTag(BUSINESS, onBatchReadyListener);

        //assert that the tagMaps are not null
        Assert.assertTrue(!tagBatchReadyListener.getTagOnBatchReadyListenerMap().isEmpty());
    }


    /**
     * Test to verify {@link TagBatchReadyListener#onReady(BatchingStrategy, TagBatchingStrategy.TagBatch)}
     */
    @Test
    public void testOnReady() {
        TagBatchReadyListener<TagData> tagBatchReadyListener;
        Tag AD = new Tag("ADS");
        Tag BUSINESS = new Tag("BUSINESS");
        Tag DEBUG = new Tag("DEBUG");

        OnBatchReadyListener<TagData, TagBatchingStrategy.TagBatch<TagData>> onBatchReadyListener = mock(OnBatchReadyListener.class);
        tagBatchReadyListener = new TagBatchReadyListener<>();
        tagBatchReadyListener.addListenerForTag(AD, onBatchReadyListener);
        tagBatchReadyListener.addListenerForTag(DEBUG, onBatchReadyListener);
        tagBatchReadyListener.addListenerForTag(BUSINESS, onBatchReadyListener);

        tagBatchReadyListener.onReady(new TagBatchingStrategy<>(), new TagBatchingStrategy.TagBatch<>(new Tag("ADS"), new SizeBatchingStrategy.SizeBatch<TagData>(Utils.fakeCollection(2), 4)));
        //verify that it gets called once , when tagBatchReadyListener's onReady gets called
        verify(onBatchReadyListener, times(1)).onReady(any(BatchingStrategy.class), any(TagBatchingStrategy.TagBatch.class));
    }
}
