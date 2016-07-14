/*
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2016 Flipkart Internet Pvt. Ltd.
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
import android.database.sqlite.SQLiteDatabase;

import com.flipkart.Utils;
import com.flipkart.batching.Batch;
import com.flipkart.batching.BatchManager;
import com.flipkart.batching.BuildConfig;
import com.flipkart.batching.Data;
import com.flipkart.batching.data.EventData;
import com.flipkart.batching.exception.DeserializeException;
import com.flipkart.batching.exception.SerializeException;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Collection;

import static junit.framework.Assert.assertEquals;

/**
 * Test for {@link DatabaseHelper}
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class DatabaseHelperTest {

    /**
     * Test to verify the {@link DatabaseHelper#addData(Collection)} adds the data in the db
     *
     * @throws DeserializeException
     * @throws SerializeException
     */
    @Test
    public void testAddData() throws DeserializeException, SerializeException {
        DatabaseHelper<Data, Batch<Data>> databaseHelper;
        Context context;
        GsonSerializationStrategy<Data, Batch<Data>> gsonSerializationStrategy;

        gsonSerializationStrategy = new GsonSerializationStrategy<>();
        BatchManager.registerBuiltInTypes(gsonSerializationStrategy);
        gsonSerializationStrategy.build();
        context = RuntimeEnvironment.application;
        databaseHelper = new DatabaseHelper<>(gsonSerializationStrategy, "test", context);
        Collection<Data> dataArrayList = Utils.fakeCollection(5);
        databaseHelper.addData(dataArrayList);
        //verify that the data retrieved from the database is similar to the inserted data.
        assertEquals(databaseHelper.getAllData(), dataArrayList);
    }

    /**
     * Test to verify the {@link DatabaseHelper#getAllData()} return the data from the db
     *
     * @throws DeserializeException
     * @throws SerializeException
     */

    @Test
    public void testGetAllData() throws DeserializeException, SerializeException {
        DatabaseHelper<Data, Batch<Data>> databaseHelper;
        Context context;
        GsonSerializationStrategy<Data, Batch<Data>> gsonSerializationStrategy;

        gsonSerializationStrategy = new GsonSerializationStrategy<>();
        BatchManager.registerBuiltInTypes(gsonSerializationStrategy);
        gsonSerializationStrategy.build();

        context = RuntimeEnvironment.application;
        databaseHelper = new DatabaseHelper<>(gsonSerializationStrategy, "test", context);
        Collection<Data> dataArrayList = Utils.fakeCollection(5);
        databaseHelper.addData(dataArrayList);

        //verify that database contains the data, after reinitializing the DatabaseHelper
        assertEquals(databaseHelper.getAllData(), dataArrayList);
    }

    /**
     * Test to verify the {@link DatabaseHelper#deleteDataList(Collection)} deletes the given list of data from db
     *
     * @throws SerializeException
     * @throws DeserializeException
     */
    @Test
    public void testDeleteParticularData() throws SerializeException, DeserializeException {
        DatabaseHelper<Data, Batch<Data>> databaseHelper;
        Context context;
        GsonSerializationStrategy<Data, Batch<Data>> gsonSerializationStrategy;

        gsonSerializationStrategy = new GsonSerializationStrategy<>();
        BatchManager.registerBuiltInTypes(gsonSerializationStrategy);
        gsonSerializationStrategy.build();
        context = RuntimeEnvironment.application;
        databaseHelper = new DatabaseHelper<>(gsonSerializationStrategy, "test", context);
        Collection<Data> dataArrayList = Utils.fakeCollection(5);
        databaseHelper.addData(dataArrayList);

        databaseHelper = new DatabaseHelper<>(gsonSerializationStrategy, " test", context);
        databaseHelper.deleteDataList(dataArrayList);

        //verify that the data has been deleted from the database.
        assertEquals(databaseHelper.getAllData().size(), 0);
    }

    /**
     * Test to verify the {@link DatabaseHelper#deleteAll()} deletes all the entries from the db
     *
     * @throws SerializeException
     * @throws DeserializeException
     */
    @Test
    public void testDeleteAllData() throws SerializeException, DeserializeException {
        DatabaseHelper<Data, Batch<Data>> databaseHelper;
        Context context;
        GsonSerializationStrategy<Data, Batch<Data>> gsonSerializationStrategy;

        gsonSerializationStrategy = new GsonSerializationStrategy<>();
        BatchManager.registerBuiltInTypes(gsonSerializationStrategy);
        gsonSerializationStrategy.build();
        context = RuntimeEnvironment.application;
        databaseHelper = new DatabaseHelper<>(gsonSerializationStrategy, "test", context);
        Collection<Data> dataArrayList = Utils.fakeCollection(5);
        databaseHelper.addData(dataArrayList);

        databaseHelper = new DatabaseHelper<>(gsonSerializationStrategy, "test", context);
        databaseHelper.deleteAll();

        //verify that the data has been deleted from the database.
        assertEquals(databaseHelper.getAllData().size(), 0);
    }

    /**
     * Test to verify the {@link DatabaseHelper#isDataInDB(String)} returns true if data is present in db.
     *
     * @throws SerializeException
     */
    @Test
    public void testIfDataInDB() throws SerializeException {
        DatabaseHelper<Data, Batch<Data>> databaseHelper;
        Context context;
        GsonSerializationStrategy<Data, Batch<Data>> gsonSerializationStrategy;

        gsonSerializationStrategy = new GsonSerializationStrategy<>();
        BatchManager.registerBuiltInTypes(gsonSerializationStrategy);
        gsonSerializationStrategy.build();
        context = RuntimeEnvironment.application;
        databaseHelper = new DatabaseHelper<>(gsonSerializationStrategy, "test", context);
        ArrayList<Data> dataArrayList = new ArrayList<>();
        Data eventData = new EventData();
        dataArrayList.add(eventData);
        databaseHelper.addData(dataArrayList);
        databaseHelper = new DatabaseHelper<>(gsonSerializationStrategy, "test", context);
        //verify that data is already present in the database.
        Assert.assertTrue(databaseHelper.isDataInDB(String.valueOf(dataArrayList.get(0).getEventId())));
    }

    /**
     * Test to verify the {@link DatabaseHelper#onUpgrade(SQLiteDatabase, int, int)}
     */
    @Test
    public void testOnUpgrade() {
        Context context;
        GsonSerializationStrategy<Data, Batch<Data>> gsonSerializationStrategy;

        gsonSerializationStrategy = new GsonSerializationStrategy<>();
        BatchManager.registerBuiltInTypes(gsonSerializationStrategy);
        gsonSerializationStrategy.build();
        context = RuntimeEnvironment.application;
        DatabaseHelper databaseHelper = new DatabaseHelper<>(gsonSerializationStrategy, "test", context);
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        databaseHelper.onUpgrade(db, 1, 2);
    }
}
