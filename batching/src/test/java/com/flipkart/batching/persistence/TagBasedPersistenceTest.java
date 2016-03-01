package com.flipkart.batching.persistence;

import android.content.Context;

import com.flipkart.Utils;
import com.flipkart.batching.Batch;
import com.flipkart.batching.BuildConfig;
import com.flipkart.batching.data.Tag;
import com.flipkart.batching.data.TagData;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by anirudh.r on 12/02/16.
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class TagBasedPersistenceTest {

    private TagBasedPersistenceStrategy<TagData> tagBasedPersistenceStrategy;
    private PersistenceStrategy<TagData> inMemoryPersistenceStrategy;
    private PersistenceStrategy<TagData> sqlPersistenceStrategy;
    private Tag ad_tag = new Tag("AD");
    private Tag debug_tag = new Tag("DEBUG");
    private Tag buisness_tag = new Tag("BUISNESS");

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test to verify {@link TagBasedPersistenceStrategy#add(Collection)}
     */
    @Test
    public void testAddData() {
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
        tagBasedPersistenceStrategy = new TagBasedPersistenceStrategy<>(tag, persistenceStrategy);
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
        Context context = RuntimeEnvironment.application;
        inMemoryPersistenceStrategy = new InMemoryPersistenceStrategy<>();
        sqlPersistenceStrategy = new SQLPersistenceStrategy<>(new ByteArraySerializationStrategy<TagData, Batch<TagData>>(), "test", context);
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
        Context context = RuntimeEnvironment.application;
        inMemoryPersistenceStrategy = new InMemoryPersistenceStrategy<>();
        sqlPersistenceStrategy = new SQLPersistenceStrategy<>(new ByteArraySerializationStrategy<TagData, Batch<TagData>>(), "test", context);
        sqlPersistenceStrategy.onInitialized();
        tagBasedPersistenceStrategy = new TagBasedPersistenceStrategy<>(ad_tag, inMemoryPersistenceStrategy);
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
        Context context = RuntimeEnvironment.application;
        inMemoryPersistenceStrategy = new InMemoryPersistenceStrategy<>();
        sqlPersistenceStrategy = new SQLPersistenceStrategy<>(new ByteArraySerializationStrategy<TagData, Batch<TagData>>(), "test", context);
        sqlPersistenceStrategy.onInitialized();
        tagBasedPersistenceStrategy = new TagBasedPersistenceStrategy<>(ad_tag, inMemoryPersistenceStrategy);
        tagBasedPersistenceStrategy = new TagBasedPersistenceStrategy<>(debug_tag, sqlPersistenceStrategy);
        tagBasedPersistenceStrategy.onInitialized();
    }
}
