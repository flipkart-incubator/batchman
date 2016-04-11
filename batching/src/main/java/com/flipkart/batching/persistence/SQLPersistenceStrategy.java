package com.flipkart.batching.persistence;

import android.content.Context;

import com.flipkart.batching.Batch;
import com.flipkart.batching.Data;
import com.flipkart.batching.exception.DeserializeException;
import com.flipkart.batching.exception.SerializeException;

import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * SQLPersistenceStrategy extends {@link InMemoryPersistenceStrategy} which is an implementation
 * of {@link PersistenceStrategy}. This persistence strategy persists the data in SQL Database and
 * sync the data on initialization of this strategy using the {@link #syncData()} method. Constructor
 * and all the overridden methods must call super method, to initialize and perform operations on
 * InMemory data list.
 */

public class SQLPersistenceStrategy<E extends Data> extends InMemoryPersistenceStrategy<E> {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SQLPersistenceStrategy.class);
    private DatabaseHelper<E, ? extends Batch> databaseHelper;
    private SerializationStrategy<E, ? extends Batch> serializationStrategy;
    private String databaseName;
    private Context context;

    public SQLPersistenceStrategy(SerializationStrategy<E, ? extends Batch> serializationStrategy, String databaseName, Context context) {
        super();
        this.serializationStrategy = serializationStrategy;
        this.databaseName = databaseName;
        this.context = context;
    }

    @Override
    public boolean add(final Collection<E> dataCollection) {
        super.add(dataCollection);
        try {
            databaseHelper.addData(dataCollection);
        } catch (SerializeException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getLocalizedMessage());
            }
        }
        return true;
    }

    /**
     * This method is called from constructor, when instance of {@link SQLPersistenceStrategy} is
     * initialized. The InMemory data list is updated with the persisted {@link Data} objects
     * which were not batched before.
     */

    private void syncData() {
        try {
            super.add(databaseHelper.getAllData());
        } catch (DeserializeException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getLocalizedMessage());
            }
        }
    }

    @Override
    public boolean removeData(final Collection<E> dataCollection) {
        super.removeData(dataCollection);
        databaseHelper.deleteAll();
        return true;
    }

    @Override
    public void onInitialized() {
        if (!isInitialized()) {
            this.databaseHelper = new DatabaseHelper<>(serializationStrategy, databaseName, context);
            syncData();
        }
        super.onInitialized();
    }
}
