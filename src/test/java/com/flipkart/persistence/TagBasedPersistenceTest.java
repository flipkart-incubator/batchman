package com.flipkart.persistence;

import com.flipkart.Utils;
import com.flipkart.batching.BuildConfig;
import com.flipkart.data.Data;
import com.flipkart.data.Tag;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

/**
 * Created by anirudh.r on 12/02/16.
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class TagBasedPersistenceTest {

    private TagBasedPersistenceStrategy tagBasedPersistenceStrategy;
    private PersistenceStrategy persistenceStrategy;
    private Tag ad_tag = new Tag("ADS");
    private Tag debug_tag = new Tag("DEBUG");
    private Tag buisness_tag = new Tag("BUISNESS");

    @Test
    public void testAddData() {
        tagBasedPersistenceStrategy = new TagBasedPersistenceStrategy(ad_tag, new InMemoryPersistenceStrategy());
        tagBasedPersistenceStrategy = new TagBasedPersistenceStrategy(debug_tag, new InMemoryPersistenceStrategy());

        ArrayList<Data> fakeCollection = Utils.fakeCollection(5);
        tagBasedPersistenceStrategy.add(fakeCollection);

//        Assert.assertTrue(tagBasedPersistenceStrategy.getData() == fakeCollection);
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
