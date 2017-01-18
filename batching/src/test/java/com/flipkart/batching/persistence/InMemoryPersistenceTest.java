/*
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2017 Flipkart Internet Pvt. Ltd.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package com.flipkart.batching.persistence;

import com.flipkart.Utils;
import com.flipkart.batching.core.Data;

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
