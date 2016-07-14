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

import android.content.Context;

import com.flipkart.Utils;
import com.flipkart.batching.BaseTestClass;
import com.flipkart.batching.BatchManager;
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
        BatchManager.registerBuiltInTypes(serializationStrategy);
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
        persistenceStrategy = new SQLPersistenceStrategy<>(new GsonSerializationStrategy<>(), createRandomString(), context);
        persistenceStrategy.onInitialized();
        persistenceStrategy.add(Utils.fakeCollection(4));
    }
}
