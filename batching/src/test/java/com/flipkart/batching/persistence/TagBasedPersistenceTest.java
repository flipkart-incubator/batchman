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

package com.flipkart.batching.persistence;

import android.content.Context;

import com.flipkart.Utils;
import com.flipkart.batching.BuildConfig;
import com.flipkart.batching.core.Batch;
import com.flipkart.batching.core.SerializationStrategy;
import com.flipkart.batching.core.data.Tag;
import com.flipkart.batching.core.data.TagData;
import com.flipkart.batching.gson.GsonSerializationStrategy;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Test for {@link TagBasedPersistenceStrategy}
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class TagBasedPersistenceTest {

    /**
     * Test to verify {@link TagBasedPersistenceStrategy#add(Collection)}
     */
    @Test
    public void testAddData() {
        TagBasedPersistenceStrategy<TagData> tagBasedPersistenceStrategy;
        PersistenceStrategy<TagData> inMemoryPersistenceStrategy;
        PersistenceStrategy<TagData> sqlPersistenceStrategy;
        Tag ad_tag = new Tag("AD");
        Tag debug_tag = new Tag("DEBUG");

        Context context = RuntimeEnvironment.application;
        inMemoryPersistenceStrategy = new InMemoryPersistenceStrategy<>();
        sqlPersistenceStrategy = new SQLPersistenceStrategy<>(new GsonSerializationStrategy<TagData, Batch<TagData>>(), "test", context);
        sqlPersistenceStrategy.onInitialized();
        tagBasedPersistenceStrategy = new TagBasedPersistenceStrategy<>(ad_tag, inMemoryPersistenceStrategy);
        tagBasedPersistenceStrategy = new TagBasedPersistenceStrategy<>(debug_tag, sqlPersistenceStrategy);

        ArrayList<TagData> fakeAdsCollection = Utils.fakeTagAdsCollection(4);
        ArrayList<TagData> fakeDebugCollection = Utils.fakeTagDebugCollection(4);
        ArrayList<TagData> arrayList = new ArrayList<>();
        arrayList.addAll(fakeAdsCollection);
        arrayList.addAll(fakeDebugCollection);
        tagBasedPersistenceStrategy.add(fakeAdsCollection);

        Assert.assertEquals(inMemoryPersistenceStrategy.getData(), fakeAdsCollection);
    }

    /**
     * Test to verify {@link TagBasedPersistenceStrategy#onInitialized()}
     *
     * @param tag
     * @param persistenceStrategy
     */
    private void initializeTagBasedPersistence(Tag tag, PersistenceStrategy<TagData> persistenceStrategy) {
        new TagBasedPersistenceStrategy<>(tag, persistenceStrategy);
    }

    /**
     * Test to verify that exception is thrown when {@link Tag} is null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIfTagNullException() {
        initializeTagBasedPersistence(null, new InMemoryPersistenceStrategy<TagData>());
    }

    /**
     * Test to verify that exception is thrown when {@link PersistenceStrategy} is null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIfPersistenceNullException() {
        initializeTagBasedPersistence(new Tag("u1"), null);
    }

    /**
     * Test to verify {@link TagBasedPersistenceStrategy#getData()}
     */
    @Test
    public void testGetData() {
        TagBasedPersistenceStrategy<TagData> tagBasedPersistenceStrategy;
        PersistenceStrategy<TagData> inMemoryPersistenceStrategy;
        PersistenceStrategy<TagData> sqlPersistenceStrategy;
        Tag ad_tag = new Tag("AD");
        Tag debug_tag = new Tag("DEBUG");

        Context context = RuntimeEnvironment.application;
        inMemoryPersistenceStrategy = new InMemoryPersistenceStrategy<>();
        SerializationStrategy serializationStrategy = new GsonSerializationStrategy();
        serializationStrategy.build();
        sqlPersistenceStrategy = new SQLPersistenceStrategy<>(serializationStrategy, "test", context);
        sqlPersistenceStrategy.onInitialized();
        tagBasedPersistenceStrategy = new TagBasedPersistenceStrategy<>(ad_tag, inMemoryPersistenceStrategy);
        tagBasedPersistenceStrategy = new TagBasedPersistenceStrategy<>(debug_tag, sqlPersistenceStrategy);
        ArrayList<TagData> fakeAdsCollection = Utils.fakeTagAdsCollection(4);
        ArrayList<TagData> fakeDebugCollection = Utils.fakeTagDebugCollection(4);
        ArrayList<TagData> arrayList = new ArrayList<>();
        arrayList.addAll(fakeAdsCollection);
        arrayList.addAll(fakeDebugCollection);
        tagBasedPersistenceStrategy.add(arrayList);

        Assert.assertNotNull(tagBasedPersistenceStrategy.getData());
    }

    /**
     * Test to verify {@link TagBasedPersistenceStrategy#removeData(Collection)}
     */
    @Test
    public void testRemoveData() {
        TagBasedPersistenceStrategy<TagData> tagBasedPersistenceStrategy;
        PersistenceStrategy<TagData> inMemoryPersistenceStrategy;
        PersistenceStrategy<TagData> sqlPersistenceStrategy;
        Tag ad_tag = new Tag("AD");
        Tag debug_tag = new Tag("DEBUG");

        Context context = RuntimeEnvironment.application;
        inMemoryPersistenceStrategy = new InMemoryPersistenceStrategy<>();
        SerializationStrategy serializationStrategy = new GsonSerializationStrategy();
        serializationStrategy.build();

        sqlPersistenceStrategy = new SQLPersistenceStrategy<>(serializationStrategy, "test", context);
        sqlPersistenceStrategy.onInitialized();
        tagBasedPersistenceStrategy = new TagBasedPersistenceStrategy<>(debug_tag, sqlPersistenceStrategy);
        ArrayList<TagData> fakeAdsCollection = Utils.fakeTagAdsCollection(4);
        ArrayList<TagData> fakeDebugCollection = Utils.fakeTagDebugCollection(4);
        ArrayList<TagData> arrayList = new ArrayList<>();
        arrayList.addAll(fakeAdsCollection);
        arrayList.addAll(fakeDebugCollection);
        tagBasedPersistenceStrategy.add(arrayList);
        tagBasedPersistenceStrategy.removeData(arrayList);
        junit.framework.Assert.assertTrue(tagBasedPersistenceStrategy.getData().size() == 0);
    }

    /**
     * Test to verify {@link TagBasedPersistenceStrategy#onInitialized()}
     */
    @Test
    public void testOnInitialized() {
        TagBasedPersistenceStrategy<TagData> tagBasedPersistenceStrategy;
        PersistenceStrategy<TagData> inMemoryPersistenceStrategy;
        PersistenceStrategy<TagData> sqlPersistenceStrategy;
        Tag ad_tag = new Tag("AD");
        Tag debug_tag = new Tag("DEBUG");

        Context context = RuntimeEnvironment.application;
        inMemoryPersistenceStrategy = new InMemoryPersistenceStrategy<>();
        SerializationStrategy serializationStrategy = new GsonSerializationStrategy();
        serializationStrategy.build();

        sqlPersistenceStrategy = new SQLPersistenceStrategy<>(serializationStrategy, "test", context);
        sqlPersistenceStrategy.onInitialized();
        tagBasedPersistenceStrategy = new TagBasedPersistenceStrategy<>(debug_tag, sqlPersistenceStrategy);
        tagBasedPersistenceStrategy.onInitialized();
    }
}
