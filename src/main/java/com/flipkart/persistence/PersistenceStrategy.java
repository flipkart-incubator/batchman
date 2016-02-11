package com.flipkart.persistence;

import android.content.Context;

import com.flipkart.batching.BatchController;
import com.flipkart.data.Data;

import java.util.Collection;

/**
 * Created by kushal.sharma on 25/01/16.
 * Interface for Persistence Strategy
 */

public interface PersistenceStrategy {

    /**
     * Adds data to in memory list as well as in the persistence (db or tape ) used.
     *
     * @param data
     */
    void add(Collection<Data> data);

    /**
     * Retrieve all the event data from the array list. Should return a copy.
     *
     * @return
     */
    Collection<Data> getData();

    /**
     * Whenever the batch is ready, this method gets called and removes the event data from persistence and the in memory list
     */
    void removeData(Collection<Data> dataArrayList);

}
