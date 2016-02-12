package com.flipkart.persistence;

import android.content.Context;
import android.os.Handler;

import com.flipkart.data.Data;
import com.flipkart.exception.DeserializeException;
import com.flipkart.exception.SerializeException;

import java.util.Collection;

/**
 * SQLPersistenceStrategy extends {@link InMemoryPersistenceStrategy} which is an implementation
 * of {@link PersistenceStrategy}. This persistence strategy persists the data in SQL Database and
 * sync the data on initialization of this strategy using the {@link #syncData()} method. Constructor
 * and all the overridden methods must call super method, to initialize and perform operations on
 * InMemory data list.
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

    @Override
    public void add(final Collection<Data> dataCollection) {
        super.add(dataCollection);
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    databaseHelper.addData(dataCollection);
                } catch (SerializeException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * This method is called from constructor, when instance of {@link SQLPersistenceStrategy} is
     * initialized. The InMemory data list is updated with the persisted {@link Data} objects
     * which were not batched before.
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
    public void removeData(final Collection<Data> dataCollection) {
        super.removeData(dataCollection);
        handler.post(new Runnable() {
            @Override
            public void run() {
                databaseHelper.deleteAll();
            }
        });
    }
}
