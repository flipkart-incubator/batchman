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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.flipkart.batching.core.Batch;
import com.flipkart.batching.core.Data;
import com.flipkart.batching.core.SerializationStrategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Database Helper class that extends {@link SQLiteOpenHelper}.
 */
public class DatabaseHelper<E extends Data, T extends Batch> extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_EVENT_DATA = "tableEventData";
    private static final String KEY_ID = "id";
    private static final String KEY_DATA = "data";
    private static final String KEY_EXPIRY = "expiry";
    private static final String IS_IN_DB_WHERE_CLAUSE = KEY_ID + " = ?";
    private static final String DELETE_WHERE_CLAUSE = KEY_ID + " IN (?) ";
    private SerializationStrategy<E, T> serializationStrategy;

    public DatabaseHelper(SerializationStrategy<E, T> serializationStrategy, String databaseName, Context context) {
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
     * @throws IOException
     */
    public void addData(Collection<E> dataCollection) throws IOException {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();
            for (E data : dataCollection) {
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
     * @throws IOException
     */
    public Collection<E> getAllData() throws IOException {
        ArrayList<E> allEventData = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(TABLE_EVENT_DATA, new String[]{KEY_DATA}, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                E event = serializationStrategy.deserializeData(cursor.getBlob(0));
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
    public void deleteDataList(Collection<E> dataCollection) {
        List<String> eventIdList = new ArrayList<>(dataCollection.size());
        for (E data : dataCollection) {
            eventIdList.add(String.valueOf(data.getEventId()));
        }
        String list = TextUtils.join(",", eventIdList);
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_EVENT_DATA, DELETE_WHERE_CLAUSE, new String[]{list});
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
        Cursor cursor = db.query(TABLE_EVENT_DATA, new String[]{KEY_ID}, IS_IN_DB_WHERE_CLAUSE, new String[]{id}, null, null, null);
        boolean result = cursor.getCount() > 0;
        cursor.close();
        return result;
    }
}
