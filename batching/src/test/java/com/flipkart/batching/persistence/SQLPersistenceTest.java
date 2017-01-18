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

import android.content.Context;

import com.flipkart.Utils;
import com.flipkart.batching.BaseTestClass;
import com.flipkart.batching.BatchManager;
import com.flipkart.batching.BuildConfig;
import com.flipkart.batching.gson.GsonSerializationStrategy;
import com.flipkart.batching.core.Data;
import com.flipkart.batching.core.SerializationStrategy;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Test for {@link SQLPersistenceStrategy}
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class SQLPersistenceTest extends BaseTestClass {

    /**
     * Test to verify that data is retained in InMemoryList after {@link PersistenceStrategy#add(Collection)} is called.
     * It also verifies the {@link SQLPersistenceStrategy#syncData()}
     */
    @Test
    public void testIfDataIsPersisted() {
        PersistenceStrategy persistenceStrategy = initializeSQLPersistence();
        ArrayList<Data> data = Utils.fakeCollection(5);
        persistenceStrategy.add(data);
        //verify that the data that was added, has been persisted and it correct form
        Assert.assertEquals(data, persistenceStrategy.getData());
    }

    @Test
    public void testIfDataIsNullException() {
        PersistenceStrategy persistenceStrategy = initializeSQLPersistence();
        persistenceStrategy.add(new ArrayList<Data>());
    }

    /**
     * Test to verify that the data has been deleted from the db
     * Also verifies that {@link SQLPersistenceStrategy#syncData()} should not contain any data
     */
    @Test
    public void testIfDataIsRemoved() {
        PersistenceStrategy persistenceStrategy = initializeSQLPersistence();
        ArrayList<Data> dataArrayList = Utils.fakeCollection(5);
        persistenceStrategy.add(dataArrayList);

        //verify that the data that was added, has been persisted and it correct form
        Assert.assertEquals(dataArrayList, persistenceStrategy.getData());

        persistenceStrategy.removeData(dataArrayList);
        //verify that the data has been deleted from the persistence layer
        Assert.assertEquals(persistenceStrategy.getData().size(), 0);
    }

    /**
     * Initialize the SQLPersistenceStrategy
     */
    private PersistenceStrategy<Data> initializeSQLPersistence() {
        PersistenceStrategy<Data> persistenceStrategy;
        Context context;
        context = RuntimeEnvironment.application;
        SerializationStrategy serializationStrategy = new GsonSerializationStrategy();
        serializationStrategy.build();
        persistenceStrategy = new SQLPersistenceStrategy<>(serializationStrategy, createRandomString(), context);
        persistenceStrategy.onInitialized();

        return persistenceStrategy;
    }

    @Test(expected = Exception.class)
    public void testSerializeException() {
        PersistenceStrategy<Data> persistenceStrategy;
        Context context;
        context = RuntimeEnvironment.application;
        persistenceStrategy = new SQLPersistenceStrategy<>(new GsonSerializationStrategy<>(), createRandomFile().getPath(), context);
        persistenceStrategy.onInitialized();
        persistenceStrategy.add(Utils.fakeCollection(4));
    }
}
