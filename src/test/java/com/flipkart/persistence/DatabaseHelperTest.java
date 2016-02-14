package com.flipkart.persistence;

import android.content.Context;

import com.flipkart.Utils;
import com.flipkart.batching.BuildConfig;
import com.flipkart.data.Data;
import com.flipkart.data.EventData;
import com.flipkart.data.Tag;
import com.flipkart.exception.DeserializeException;
import com.flipkart.exception.SerializeException;

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
 * Created by anirudh.r on 12/02/16.
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class DatabaseHelperTest {

    private DatabaseHelper databaseHelper;
    private Context context;

    /**
     * Test to verify the {@link DatabaseHelper#addData(Collection)} adds the data in the db
     *
     * @throws DeserializeException
     * @throws SerializeException
     */
    @Test
    public void testAddData() throws DeserializeException, SerializeException {

        context = RuntimeEnvironment.application;
        databaseHelper = new DatabaseHelper(new GsonSerializationStrategy(), context);
        Collection<Data> dataArrayList = Utils.fakeCollection(5);
        databaseHelper.addData(dataArrayList);
        //verify that the data retrieved from the database is similar to the inserted data.
        assertEquals(databaseHelper.getAllData(), dataArrayList);
    }

    /**
     * Test to verify the {@link DatabaseHelper#getAllData()} return the data in the db
     *
     * @throws DeserializeException
     * @throws SerializeException
     */
    @Test
    public void testGetAllData() throws DeserializeException, SerializeException {
        context = RuntimeEnvironment.application;
        databaseHelper = new DatabaseHelper(new GsonSerializationStrategy(), context);
        Collection<Data> dataArrayList = Utils.fakeCollection(5);
        databaseHelper.addData(dataArrayList);

        databaseHelper = new DatabaseHelper(new GsonSerializationStrategy(), context);
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
        context = RuntimeEnvironment.application;
        databaseHelper = new DatabaseHelper(new GsonSerializationStrategy(), context);
        Collection<Data> dataArrayList = Utils.fakeCollection(5);
        databaseHelper.addData(dataArrayList);

        databaseHelper = new DatabaseHelper(new GsonSerializationStrategy(), context);
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
        context = RuntimeEnvironment.application;
        databaseHelper = new DatabaseHelper(new GsonSerializationStrategy(), context);
        Collection<Data> dataArrayList = Utils.fakeCollection(5);
        databaseHelper.addData(dataArrayList);

        databaseHelper = new DatabaseHelper(new GsonSerializationStrategy(), context);
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
        context = RuntimeEnvironment.application;
        databaseHelper = new DatabaseHelper(new GsonSerializationStrategy(), context);
        ArrayList<Data> dataArrayList = new ArrayList<>();
        Data eventData = new EventData(new Tag("u1"), "");
        dataArrayList.add(eventData);
        databaseHelper.addData(dataArrayList);
        databaseHelper = new DatabaseHelper(new GsonSerializationStrategy(), context);
        //verify that data is already present in the database.
        Assert.assertTrue(databaseHelper.isDataInDB(String.valueOf(dataArrayList.get(0).getEventId())));
    }
}
