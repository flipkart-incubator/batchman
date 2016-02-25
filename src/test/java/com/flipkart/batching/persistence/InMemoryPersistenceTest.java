package com.flipkart.batching.persistence;

import com.flipkart.Utils;
import com.flipkart.batching.Data;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by anirudh.r on 11/02/16.
 */
public class InMemoryPersistenceTest {

    InMemoryPersistenceStrategy<Data> persistenceStrategy;

    @Mock
    Data Data;

    @Before
    public void setUp() {
        persistenceStrategy = new InMemoryPersistenceStrategy<>();
    }

    /**
     * Test to verify that data is retained in InMemoryList after {@link PersistenceStrategy#add(Collection)} is called.
     */
    @Test
    public void testIfDataInMemory() {
        ArrayList<Data> data = Utils.fakeCollection(5);
        persistenceStrategy.add(data);

        Assert.assertEquals(persistenceStrategy.getData(), data);
    }

    /**
     * Test to verify that duplicate data are not being added to the persistence layer
     */
    @Test
    public void testIfDuplicateEntryNotPresent() {

        //creates unique data
        ArrayList<Data> data = Utils.fakeCollection(5);
        persistenceStrategy.add(data);
        //Persistence strategy should return only 1 Data, as all the data that were added were duplicate.
        Assert.assertEquals(persistenceStrategy.getData().size(), 5);
    }

    /**
     * Test to verify that {@link PersistenceStrategy#removeData(Collection)} is clearing the InMemoryList.
     */
    @Test
    public void testIfRemoveData() {
        ArrayList<Data> data = Utils.fakeCollection(5);
        persistenceStrategy.add(data);
        persistenceStrategy.removeData(data);
        Assert.assertTrue(persistenceStrategy.getData().size() == 0);
    }

    @Test
    public void testOnInitialized() {
        persistenceStrategy.onInitialized();
        Assert.assertTrue(persistenceStrategy.isInitialized());
    }


}
