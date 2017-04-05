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
import com.flipkart.batching.BaseTestClass;
import com.flipkart.batching.BuildConfig;
import com.flipkart.batching.core.Data;
import com.flipkart.batching.core.SerializationStrategy;
import com.flipkart.batching.gson.GsonSerializationStrategy;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Test for {@link TapePersistenceStrategy}
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
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

    @Test
    public void testIfQueueCreatedAgain() throws Exception {
        SerializationStrategy serializationStrategy = new GsonSerializationStrategy();
        serializationStrategy.build();
        TapePersistenceStrategy<Data> persistenceStrategy = new TapePersistenceStrategy<>(createRandomFile().getPath(), serializationStrategy);
        persistenceStrategy.onInitialized();
        //add data list to the persistence
        ArrayList<Data> dataArrayList = Utils.fakeCollection(1000);
        persistenceStrategy.add(dataArrayList);

        //trying to re create the queue file, which may happen due to low memory available to create the queue file
        persistenceStrategy.createInMemoryQueueFile(new IOException());

        //now try to remove the data from the queue file. Since due to the low memory, we did create an in-memory queue. This will remove the list
        //from the in-memory queue file as well.
        persistenceStrategy.removeData(dataArrayList);
    }

    /**
     * Initialize the TapePersistenceStrategy
     */
    private PersistenceStrategy<Data> initializeTapePersistence() {
        File file = createRandomFile();
        SerializationStrategy serializationStrategy = new GsonSerializationStrategy();
        serializationStrategy.build();
        TapePersistenceStrategy<Data> persistenceStrategy = new TapePersistenceStrategy<>(createRandomFile().getPath(), serializationStrategy);
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
