/*
 * Copyright 2016 Flipkart Internet Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flipkart.batching.persistence;

import com.flipkart.Utils;
import com.flipkart.batching.Data;

import junit.framework.Assert;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

/**
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
