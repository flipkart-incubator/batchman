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
 * Created by kushal.sharma on 27/01/16.
 * Database Helper Class
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

    public void addData(Collection<Data> data) throws SerializeException {
        SQLiteDatabase db = this.getWritableDatabase();
        for (Data mData : data) {
            if (!isDataInDB(String.valueOf(mData.getEventId()))) {
                ContentValues values = new ContentValues();
                values.put(KEY_ID, mData.getEventId());
                values.put(KEY_DATA, serializationStrategy.serialize(mData));
                values.put(KEY_EXPIRY, 0);
                db.insert(TABLE_EVENT_DATA, null, values);
            }
        }
        db.close();

    }

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

    public int getDataCount() {
        String countQuery = "SELECT * FROM " + TABLE_EVENT_DATA;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    public void deleteDataList(Collection<Data> dataList) {
        Log.d("db delete", "Deleted data from db " + dataList.size());

        SQLiteDatabase db = getWritableDatabase();
        for (Data data : dataList) {
            // db.execSQL("delete from " + TABLE_EVENT_DATA + " where " + KEY_ID + " = " + data.getEventId());
            db.delete(TABLE_EVENT_DATA, KEY_ID + "=" + data.getEventId(), null);
        }

        Log.d("db delete", "Deleted data from db " + dataList.size());

//        ArrayList<String> eventIds = new ArrayList<>();
//        for (Data d : dataList) eventIds.add(String.valueOf(d.getEventId()));
//        String ids = TextUtils.join(",", eventIds.toArray());
//        if (db != null) db.delete(TABLE_EVENT_DATA, KEY_ID + " IN (?)", new String[]{ids});
    }

    public void deleteAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_EVENT_DATA, null, null);
    }

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
