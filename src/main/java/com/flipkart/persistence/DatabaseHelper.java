package com.flipkart.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.flipkart.data.Data;
import com.flipkart.exception.DeserializeException;
import com.flipkart.exception.SerializeException;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Database Helper class that extends {@link SQLiteOpenHelper}.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_EVENT_DATA = "tableEventData";
    private static final String KEY_ID = "id";
    private static final String KEY_DATA = "data";
    private static final String KEY_EXPIRY = "expiry";
    private static final String WHERE_CLAUSE = KEY_ID + " = ?";
    private SerializationStrategy serializationStrategy;

    public DatabaseHelper(SerializationStrategy serializationStrategy, String databaseName, Context context) {
        super(context, databaseName, null, DATABASE_VERSION);
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
        try {
            db.beginTransaction();
            for (Data data : dataCollection) {
                if (!isDataInDB(String.valueOf(data.getEventId()))) {
                    ContentValues values = new ContentValues();
                    values.put(KEY_ID, data.getEventId());
                    values.put(KEY_DATA, serializationStrategy.serializeData(data));
                    values.put(KEY_EXPIRY, 0);
                    db.insertWithOnConflict(TABLE_EVENT_DATA, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * This method gets all the data in database and return them after deserialize.
     *
     * @return collection of {@link Data} objects
     * @throws DeserializeException
     */
    public Collection<Data> getAllData() throws DeserializeException {
        ArrayList<Data> allEventData = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(TABLE_EVENT_DATA, new String[]{KEY_DATA}, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Data event = serializationStrategy.deserializeData(cursor.getBlob(0));
                allEventData.add(event);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return allEventData;
    }

    /**
     * This method deletes the provided {@link Collection} of {@link Data} objects from the
     * database.
     *
     * @param dataCollection collection of {@link Data} objects to be deleted
     */
    public void deleteDataList(Collection<Data> dataCollection) {
        StringBuilder selectionArg = new StringBuilder();
        int idx = 0;
        for (Data data : dataCollection) {
            if (idx != 0) {
                selectionArg.append(",");
            }
            selectionArg.append(data.getEventId());
            idx++;
        }
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_EVENT_DATA, KEY_ID + " IN (?) ", new String[]{selectionArg.toString()});
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
        Cursor cursor = db.query(TABLE_EVENT_DATA, new String[]{KEY_ID}, WHERE_CLAUSE, new String[]{id}, null, null, null);
        boolean result = cursor.getCount() > 0;
        cursor.close();
        return result;
    }
}
