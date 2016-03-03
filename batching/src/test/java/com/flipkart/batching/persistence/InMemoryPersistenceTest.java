package com.flipkart.batching.persistence;

import com.flipkart.Utils;
import com.flipkart.batching.Data;

import junit.framework.Assert;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by anirudh.r on 11/02/16.
 * Test for {@link InMemoryPersistenceStrategy}
 */
public class InMemoryPersistenceTest {

    /**
     * Test to verify that data is retained in InMemoryList after {@link PersistenceStrategy#add(Collection)} is called.
     */
    @Test
    public void testIfDataInMemory() {
        InMemoryPersistenceStrategy<Data> persistenceStrategy = new InMemoryPersistenceStrategy<>();

        ArrayList<Data> data = Utils.fakeCollection(5);
        persistenceStrategy.add(data);

        Assert.assertEquals(persistenceStrategy.getData(), data);
    }

    /**
     * Test to verify that duplicate data are not being added to the persistence layer
     */
    @Test
    public void testIfDuplicateEntryNotPresent() {
        InMemoryPersistenceStrategy<Data> persistenceStrategy = new InMemoryPersistenceStrategy<>();

        //creates unique data
        ArrayList<Data> data = Utils.fakeCollection(5);
        persistenceStrategy.add(data);
        //Persistence strategy should return only 1 Data, as all the data that were added were duplicate.
        Assert.assertEquals(persistenceStrategy.getData().size(), 5);
    }

    /**
     * Test to verify that {@link PersistenceStrategy#removeData(Collection)} is clearing the inMemoryList.
     */
    @Test
    public void testIfRemoveData() {
        InMemoryPersistenceStrategy<Data> persistenceStrategy = new InMemoryPersistenceStrategy<>();
        ArrayList<Data> data = Utils.fakeCollection(5);
        persistenceStrategy.add(data);
        persistenceStrategy.removeData(data);
        Assert.assertTrue(persistenceStrategy.getData().size() == 0);
    }

    /**
     * Test to verify {@link InMemoryPersistenceStrategy#isInitialized()}
     */
    @Test
    public void testOnInitialized() {
        InMemoryPersistenceStrategy<Data> persistenceStrategy = new InMemoryPersistenceStrategy<>();
        persistenceStrategy.onInitialized();
        Assert.assertTrue(persistenceStrategy.isInitialized());
    }
}
