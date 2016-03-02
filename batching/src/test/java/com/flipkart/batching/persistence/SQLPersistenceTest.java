package com.flipkart.batching.persistence;

import android.content.Context;

import com.flipkart.Utils;
import com.flipkart.batching.BuildConfig;
import com.flipkart.batching.Data;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by anirudh.r on 11/02/16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class SQLPersistenceTest {

    private PersistenceStrategy<Data> persistenceStrategy;
    private Context context;


    /**
     * Test to verify that data is retained in InMemoryList after {@link PersistenceStrategy#add(Collection)} is called.
     * It also verifies the {@link SQLPersistenceStrategy#syncData()}
     */
    @Test
    public void testIfDataIsPersisted() {

        initializeSQLPersistence();
        ArrayList<Data> data = Utils.fakeCollection(5);
        persistenceStrategy.add(data);
        //verify that the data that was added, has been persisted and it correct form
        Assert.assertEquals(data, persistenceStrategy.getData());
    }

    @Test
    public void testIfDataIsNullException() {
        initializeSQLPersistence();
        persistenceStrategy.add(new ArrayList<Data>());
    }

    /**
     * Test to verify that the data has been deleted from the db
     * Also verifies that {@link SQLPersistenceStrategy#syncData()} should not contain any data
     */
    @Test
    public void testIfDataIsRemoved() {
        initializeSQLPersistence();
        ArrayList<Data> dataArrayList = Utils.fakeCollection(5);
        persistenceStrategy.add(dataArrayList);

        //verify that the data that was added, has been persisted and it correct form
        Assert.assertEquals(dataArrayList, persistenceStrategy.getData());

        persistenceStrategy.removeData(dataArrayList);
        //verify that the data has been deleted from the persistence layer
        Assert.assertEquals(persistenceStrategy.getData().size(), 0);
    }

    /**
     * Initialize the SQLPersistence
     */
    private void initializeSQLPersistence() {
        context = RuntimeEnvironment.application;
        persistenceStrategy = new SQLPersistenceStrategy<>(new ByteArraySerializationStrategy<>(), "test", context);
        persistenceStrategy.onInitialized();
    }

    @Test(expected = Exception.class)
    public void testSerializeException() {
        context = RuntimeEnvironment.application;
        persistenceStrategy = new SQLPersistenceStrategy<>(new GsonSerializationStrategy<>(), "test", context);
        persistenceStrategy.onInitialized();
        persistenceStrategy.add(Utils.fakeCollection(4));
    }
}
