package com.flipkart.persistence;

import com.flipkart.data.Data;
import com.flipkart.data.EventData;
import com.flipkart.data.Tag;

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

    PersistenceStrategy persistenceStrategy;

    @Mock
    Data eventData;

    @Before
    public void setUp() {
        persistenceStrategy = new InMemoryPersistenceStrategy();
    }

    /**
     * Test to verify that data is retained in InMemoryList after {@link PersistenceStrategy#add(Collection)} is called.
     */
    @Test
    public void testIfDataInMemory() {
        Data eventData = new EventData(new Tag("1"), "e1");
        Data eventData1 = new EventData(new Tag("2"), "e2");
        Data eventData2 = new EventData(new Tag("3"), "e3");
        Data eventData3 = new EventData(new Tag("4"), "e4");
        Data eventData4 = new EventData(new Tag("5"), "e5");

        ArrayList<Data> data = new ArrayList<>();
        data.add(eventData);
        data.add(eventData1);
        data.add(eventData2);
        data.add(eventData3);
        data.add(eventData4);
        persistenceStrategy.add(data);

        Assert.assertEquals(persistenceStrategy.getData().size(), data.size());
    }

    /**
     * Test to verify that duplicate data are not being added to the persistence layer
     */
    @Test
    public void testIfDuplicateEntryNotPresent() {

        ArrayList<Data> data = fakeCollection(5);
        persistenceStrategy.add(data);
        //Persistence strategy should return only 1 Data, as all the data that were added were duplicate.
        Assert.assertEquals(persistenceStrategy.getData().size(), 1);
    }

    /**
     * Test to verify that {@link PersistenceStrategy#removeData(Collection)} is clearing the InMemoryList.
     */
    @Test
    public void testIfRemoveData() {
        ArrayList<Data> data = fakeCollection(5);
        persistenceStrategy.add(data);

        persistenceStrategy.removeData(data);

        Assert.assertTrue(persistenceStrategy.getData().size() == 0);
    }

    /**
     * Method to create fake array list of Data.
     *
     * @param size
     * @return
     */
    protected ArrayList<Data> fakeCollection(int size) {
        ArrayList<Data> dataArrayList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            dataArrayList.add(eventData);
        }
        return dataArrayList;
    }

}
