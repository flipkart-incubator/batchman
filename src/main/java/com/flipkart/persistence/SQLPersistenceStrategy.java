package com.flipkart.persistence;

import android.content.Context;
import android.os.Handler;

import com.flipkart.data.Data;
import com.flipkart.exception.DeserializeException;
import com.flipkart.exception.SerializeException;

import java.util.Collection;

/**
 * Created by anirudh.r on 27/01/16.
 * SQL Persistence Strategy
 */

public class SQLPersistenceStrategy extends InMemoryPersistenceStrategy {

    private final Handler handler;
    private DatabaseHelper databaseHelper;

    public SQLPersistenceStrategy(SerializationStrategy serializationStrategy, Context context, Handler handler) {
        super();
        this.databaseHelper = new DatabaseHelper(serializationStrategy, context);
        this.handler = handler;
        syncData();
    }

    /**
     * This method adds the data event to the database and arraylist
     *
     * @param data
     */
    @Override
    public void add(final Collection<Data> data) {
        super.add(data);
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    databaseHelper.addData(data);
                } catch (SerializeException e) {
                    e.printStackTrace();
                    //TODO enable logging
                }
            }
        });
    }

    /**
     * This method gets called , whenever the app gets launched, the in memory list gets snyced from all the events stored in db.
     */
    private void syncData() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    SQLPersistenceStrategy.super.add(databaseHelper.getAllData());
                } catch (DeserializeException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void removeData(final Collection<Data> syncedData) {
        super.removeData(syncedData);
        handler.post(new Runnable() {
            @Override
            public void run() {
                databaseHelper.deleteAll();
            }
        });
    }
}
