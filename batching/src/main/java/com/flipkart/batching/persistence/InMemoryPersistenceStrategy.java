package com.flipkart.batching.persistence;

import com.flipkart.batching.Data;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A simple implementation of {@link PersistenceStrategy}.
 * This strategy persist data in an InMemory using {@link ArrayList}.
 */
public class InMemoryPersistenceStrategy<E extends Data> implements PersistenceStrategy<E> {
    protected ArrayList<E> dataList = new ArrayList<>();
    private boolean initialized;

    /**
     * Returns the initialization status.
     *
     * @return true if initialized
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Adds the collection of data to InMemory list.
     *
     * @param dataCollection collection of {@link Data} objects
     * @return true if list is edited
     */
    @Override
    public boolean add(Collection<E> dataCollection) {
        boolean isAdded = false;
        for (E data : dataCollection) {
            if (null != data) {
                dataList.add(data);
                isAdded = true;
            }
        }
        return isAdded;
    }

    /**
     * Adds the provided data object to InMemory List
     *
     * @param data data object
     */

    public void add(E data) {
        dataList.add(data);
    }

    /**
     * This method returns data stored in InMemory data list
     *
     * @return collection of data objects present InMemory
     */

    @Override
    public Collection<E> getData() {
        return new ArrayList<>(dataList);
    }

    @Override
    public int getDataSize() {
        return dataList.size();
    }

    /**
     * Removes provided collection of data from InMemory list.
     *
     * @param dataCollection collection of {@link Data} objects
     */

    @Override
    public void removeData(Collection<E> dataCollection) {
        dataList.removeAll(dataCollection);
    }

    /**
     * Sets initialized to true.
     */

    @Override
    public void onInitialized() {
        initialized = true;
    }
}
