package com.flipkart.persistence;

import android.content.Context;

import com.flipkart.Utils;
import com.flipkart.batching.BuildConfig;
import com.flipkart.data.Data;
import com.flipkart.data.Tag;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

/**
 * Created by anirudh.r on 12/02/16.
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class TagBasedPersistenceTest {

    private TagBasedPersistenceStrategy tagBasedPersistenceStrategy;
    private PersistenceStrategy inMemoryPersistenceStrategy;
    private PersistenceStrategy sqlPersistenceStrategy;
    private Tag ad_tag = new Tag("AD");
    private Tag debug_tag = new Tag("DEBUG");
    private Tag buisness_tag = new Tag("BUISNESS");

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAddData() {
        Context context = RuntimeEnvironment.application;
        inMemoryPersistenceStrategy = new InMemoryPersistenceStrategy();
        sqlPersistenceStrategy = new SQLPersistenceStrategy(new GsonSerializationStrategy(), context);
        sqlPersistenceStrategy.onInitialized();
        tagBasedPersistenceStrategy = new TagBasedPersistenceStrategy(ad_tag, inMemoryPersistenceStrategy);
        tagBasedPersistenceStrategy = new TagBasedPersistenceStrategy(debug_tag, sqlPersistenceStrategy);

        ArrayList<Data> fakeAdsCollection = Utils.fakeAdsCollection(4);
        ArrayList<Data> fakeDebugCollection = Utils.fakeDebugCollection(4);
        ArrayList<Data> arrayList = new ArrayList<>();
        arrayList.addAll(fakeAdsCollection);
        arrayList.addAll(fakeDebugCollection);
        tagBasedPersistenceStrategy.add(fakeAdsCollection);

        Assert.assertEquals(inMemoryPersistenceStrategy.getData(), fakeAdsCollection);
    }

    private void initializeTagBasedPersistence(Tag tag, PersistenceStrategy persistenceStrategy) {
        tagBasedPersistenceStrategy = new TagBasedPersistenceStrategy(tag, persistenceStrategy);
    }

    /**
     * Test to verify that exception is thrown when {@link Tag} is null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIfTagNullException() {
        initializeTagBasedPersistence(null, new InMemoryPersistenceStrategy());
    }

    /**
     * Test to verify that exception is thrown when {@link PersistenceStrategy} is null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIfPersistenceNullException() {
        initializeTagBasedPersistence(new Tag("u1"), null);
    }
}
