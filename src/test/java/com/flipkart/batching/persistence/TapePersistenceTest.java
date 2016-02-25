package com.flipkart.batching.persistence;

import com.flipkart.Utils;
import com.flipkart.batching.Batch;
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
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class TapePersistenceTest {

    private File file;
    private SerializationStrategy<Data, Batch<Data>> serializationStrategy;
    private TapePersistenceStrategy<Data> persistenceStrategy;

    @Test
    public void testIfDataIsPersisted() {
        initializeTapePersistence();
        ArrayList<Data> data = Utils.fakeCollection(5);
        persistenceStrategy.add(data);
        //verify that the data that was added, has been persisted and it correct form
        Assert.assertEquals(data, persistenceStrategy.getData());

        initializeTapePersistence();
        Assert.assertEquals(data, persistenceStrategy.getData());
    }

    @Test
    public void testIfDataIsNullException() {
        initializeTapePersistence();
        persistenceStrategy.add(new ArrayList<Data>());
    }

    /**
     * Test to verify that the data has been deleted from the db
     * Also verifies that {@link TapePersistenceStrategy#syncData()} should not contain any data
     */
    @Test
    public void testIfDataIsRemoved() {
        initializeTapePersistence();
        ArrayList<Data> dataArrayList = Utils.fakeCollection(5);
        persistenceStrategy.add(dataArrayList);

        //verify that the data that was added, has been persisted and it correct form
        Assert.assertEquals(dataArrayList, persistenceStrategy.getData());

        persistenceStrategy.removeData(dataArrayList);
        //verify that the data has been deleted from the persistence layer
        Assert.assertEquals(persistenceStrategy.getData().size(), 0);
    }

    @Test
    public void testInsertHugeData() {
        initializeTapePersistence();
        ArrayList<Data> dataArrayList = Utils.fakeCollection(100000);
        persistenceStrategy.add(dataArrayList);

        //verify that the data that was added, has been persisted and it correct form
        Assert.assertEquals(dataArrayList, persistenceStrategy.getData());
    }

    /**
     * Initialize the TapePersistence
     */
    private void initializeTapePersistence() {
        file = new File("test_file");
        serializationStrategy = new ByteArraySerializationStrategy<>();
        persistenceStrategy = new TapePersistenceStrategy<>(file, serializationStrategy);
        persistenceStrategy.onInitialized();
    }

    @After
    public void afterTest() {
        file.delete();
    }
}
