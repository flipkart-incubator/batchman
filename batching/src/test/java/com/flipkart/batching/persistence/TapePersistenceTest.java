package com.flipkart.batching.persistence;

import com.flipkart.Utils;
import com.flipkart.batching.BaseTestClass;
import com.flipkart.batching.BatchManager;
import com.flipkart.batching.BuildConfig;
import com.flipkart.batching.Data;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by anirudh.r on 25/02/16.
 * Test for {@link TapePersistenceStrategy}
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class TapePersistenceTest extends BaseTestClass {

    /**
     * Test to verify if data is persisted
     * Verifies {@link TapePersistenceStrategy#syncData()} when it gets initialized
     */
    @Test
    public void testIfDataIsPersisted() {
        PersistenceStrategy persistenceStrategy = initializeTapePersistence();
        ArrayList<Data> data = Utils.fakeCollection(5);
        persistenceStrategy.add(data);
        //verify that the data that was added, has been persisted and it correct form
        Assert.assertEquals(data, persistenceStrategy.getData());
    }

    @Test
    public void testIfDataIsNullException() {
        PersistenceStrategy persistenceStrategy = initializeTapePersistence();
        persistenceStrategy.add(new ArrayList<Data>());
    }

    /**
     * Test to verify that the data has been deleted from the tape
     * Also verifies that {@link TapePersistenceStrategy#syncData()} should not contain any data
     */
    @Test
    public void testIfDataIsRemoved() {
        PersistenceStrategy persistenceStrategy = initializeTapePersistence();
        ArrayList<Data> dataArrayList = Utils.fakeCollection(5);
        persistenceStrategy.add(dataArrayList);

        //verify that the data that was added, has been persisted and it correct form
        Assert.assertEquals(dataArrayList, persistenceStrategy.getData());

        persistenceStrategy.removeData(dataArrayList);
        //verify that the data has been deleted from the persistence layer
        Assert.assertEquals(persistenceStrategy.getData().size(), 0);
    }

    /**
     * Test to verify the performance of tape by ingesting high range of data to it
     */
    @Test
    public void testInsertHugeData() {
        PersistenceStrategy persistenceStrategy = initializeTapePersistence();
        ArrayList<Data> dataArrayList = Utils.fakeCollection(1000);
        persistenceStrategy.add(dataArrayList);

        //verify that the data that was added, has been persisted and it correct form
        Assert.assertEquals(dataArrayList, persistenceStrategy.getData());
    }

    /**
     * Initialize the TapePersistenceStrategy
     */
    private PersistenceStrategy<Data> initializeTapePersistence() {
        File file = createRandomFile();
        SerializationStrategy serializationStrategy = new GsonSerializationStrategy();
        BatchManager.registerBuiltInTypes(serializationStrategy);
        serializationStrategy.build();
        TapePersistenceStrategy<Data> persistenceStrategy = new TapePersistenceStrategy<>(createRandomString(), serializationStrategy);
        persistenceStrategy.onInitialized();

        return persistenceStrategy;
    }

    /**
     * Delete all the test_files when the test ends
     */
    @After
    public void afterTest() {
        deleteRandomFiles();
    }
}
