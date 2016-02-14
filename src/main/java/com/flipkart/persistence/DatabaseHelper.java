package com.flipkart.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.flipkart.data.Data;
import com.flipkart.data.EventData;
import com.flipkart.exception.DeserializeException;
import com.flipkart.exception.SerializeException;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Database Helper class that extends {@link SQLiteOpenHelper}.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "dataManager";
    public static final String TABLE_EVENT_DATA = "tableEventData";
    public static final String KEY_ID = "id";
    public static final String KEY_DATA = "data";
    public static final String KEY_EXPIRY = "expiry";
    private SerializationStrategy serializationStrategy;

    public DatabaseHelper(SerializationStrategy serializationStrategy, Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.serializationStrategy = serializationStrategy;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_EVENT_DATA = "CREATE TABLE "
                + TABLE_EVENT_DATA + "("
                + KEY_ID + " TEXT PRIMARY KEY,"
                + KEY_DATA + " BLOB,"
                + KEY_EXPIRY + " INTEGER" + ")";
        db.execSQL(CREATE_TABLE_EVENT_DATA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENT_DATA);
        onCreate(db);
    }

    /**
     * This method serialize the provided collection of {@link Data} objects and save them to
     * the database.
     *
     * @param dataCollection collection of {@link Data} objects.
     * @throws SerializeException
     */

    public void addData(Collection<Data> dataCollection) throws SerializeException {
        SQLiteDatabase db = this.getWritableDatabase();
        for (Data data : dataCollection) {
            if (!isDataInDB(String.valueOf(data.getEventId()))) {
                ContentValues values = new ContentValues();
                values.put(KEY_ID, data.getEventId());
                values.put(KEY_DATA, serializationStrategy.serialize(data));
                values.put(KEY_EXPIRY, 0);
                db.insert(TABLE_EVENT_DATA, null, values);
            }
        }
        db.close();

    }

    /**
     * This method gets all the data in database and return them after deserialize.
     *
     * @return collection of {@link Data} objects
     * @throws DeserializeException
     */

    public Collection<Data> getAllData() throws DeserializeException {
        ArrayList<Data> allEventData = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_EVENT_DATA;
        SQLiteDatabase db = getWritableDatabase();
        Data event;
        if (db != null) {
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    event = (EventData) serializationStrategy.deserialize(cursor.getBlob(1));
                    allEventData.add(event);
                } while (cursor.moveToNext());
            }
            cursor.close();
            return allEventData;
        }
        return null;
    }

    /**
     * This method deletes the provided {@link Collection} of {@link Data} objects from the
     * database.
     *
     * @param dataCollection collection of {@link Data} objects to be deleted
     */

    public void deleteDataList(Collection<Data> dataCollection) {
        Log.d("db delete", "Deleted data from db " + dataCollection.size());

        SQLiteDatabase db = getWritableDatabase();
        for (Data data : dataCollection) {
            db.delete(TABLE_EVENT_DATA, KEY_ID + "=" + data.getEventId(), null);
        }
    }

    /**
     * This method deletes all the data in the database.
     */

    public void deleteAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_EVENT_DATA, null, null);
    }

    /**
     * This method returns true if the given id is present in the database.
     *
     * @param id eventId of {@link Data} object
     * @return true is {@link Data} object is in database
     */

    public boolean isDataInDB(String id) {
        SQLiteDatabase db = getReadableDatabase();
        String Query = "Select * from " + TABLE_EVENT_DATA + " where " + KEY_ID + " = " + id;
        Cursor cursor = db.rawQuery(Query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }
}
