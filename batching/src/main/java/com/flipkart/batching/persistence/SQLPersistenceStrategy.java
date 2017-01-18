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

import android.content.Context;

import com.flipkart.batching.core.Batch;
import com.flipkart.batching.core.Data;
import com.flipkart.batching.core.SerializationStrategy;
import com.flipkart.batching.core.exception.DeserializeException;
import com.flipkart.batching.core.exception.SerializeException;

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
    public void removeData(final Collection<E> dataCollection) {
        super.removeData(dataCollection);
        databaseHelper.deleteAll();
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
