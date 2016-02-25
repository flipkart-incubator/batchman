package com.flipkart.batching;

import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.flipkart.batching.data.EventData;
import com.flipkart.batching.data.Tag;
import com.flipkart.batching.exception.DeserializeException;
import com.flipkart.batching.exception.SerializeException;
import com.flipkart.batching.persistence.ByteArraySerializationStrategy;
import com.flipkart.batching.persistence.DatabaseHelper;
import com.flipkart.batching.persistence.SerializationStrategy;

import java.util.ArrayList;

/**
 * Created by anirudh.r on 11/02/16.
 */
public class DBHelperTest extends AndroidTestCase {

    private DatabaseHelper databaseHelper;

    SerializationStrategy serializationStrategy;
    SQLiteDatabase database;
    private static String DATA = "Event 2";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        serializationStrategy = new ByteArraySerializationStrategy();
        databaseHelper = new DatabaseHelper(serializationStrategy, mContext);
        database = databaseHelper.getWritableDatabase();
    }

    /**
     * Test to drop the database
     */
    public void testDropDB() {
        assertTrue(mContext.deleteDatabase(DatabaseHelper.DATABASE_NAME));
    }

    /**
     * Test to create the database.
     */
    public void testCreateDB() {
        DatabaseHelper dbHelper = new DatabaseHelper(serializationStrategy, mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        assertTrue(db.isOpen());
    }

    /**
     * Insert data events into the database
     *
     * @throws Exception
     */
    public void testInsertUniqueData() throws Exception {

        int numberOfEvents = 5;

        Data eventData = new EventData(new Tag("u1"), DATA);
        Data eventData1 = new EventData(new Tag("u2"), DATA);
        Data eventData2 = new EventData(new Tag("u3"), DATA);
        Data eventData3 = new EventData(new Tag("u4"), DATA);
        Data eventData4 = new EventData(new Tag("u5"), DATA);

        ArrayList<Data> dataArrayList = new ArrayList<>();
        dataArrayList.add(eventData);
        dataArrayList.add(eventData1);
        dataArrayList.add(eventData2);
        dataArrayList.add(eventData3);
        dataArrayList.add(eventData4);

        databaseHelper.addData(dataArrayList);

        assertEquals(databaseHelper.getAllData().size(), numberOfEvents);
    }

    /**
     * Test to verify that duplicate data events are not added to the database.
     *
     * @throws SerializeException
     * @throws DeserializeException
     */
    public void testIfDuplicateEntryNotAdded() throws SerializeException, DeserializeException {

        int numberOfEvents = 5;
        Data eventData = new EventData(new Tag("u1"), "Event 1");

        ArrayList<Data> dataArrayList = new ArrayList<>();
        dataArrayList.add(eventData);
        dataArrayList.add(eventData);
        dataArrayList.add(eventData);
        dataArrayList.add(eventData);
        dataArrayList.add(eventData);

        databaseHelper.addData(dataArrayList);

        assertEquals(databaseHelper.getAllData().size(), 1);
    }

    /**
     * Test to verify the data stored in database is correct
     *
     * @throws Exception
     */
    public void testIsDataCorrectInDB() throws Exception {
        ArrayList<Data> arrayListDB = (ArrayList<Data>) databaseHelper.getAllData();
        for (Data d : arrayListDB) {
            assertEquals(d.getData(), DATA);
        }
    }

    /**
     * Test to delete the data from the database.
     *
     * @throws Exception
     */
    public void testDeleteData() throws Exception {
        ArrayList<Data> arrayListDB = (ArrayList<Data>) databaseHelper.getAllData();
        databaseHelper.deleteDataList(arrayListDB);
    }
}
