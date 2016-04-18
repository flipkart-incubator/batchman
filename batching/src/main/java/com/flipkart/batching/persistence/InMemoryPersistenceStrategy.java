package com.flipkart.batching.persistence;

import com.flipkart.batching.Data;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This is a simple implementation of {@link PersistenceStrategy}.
 * This strategy persist data in an InMemory {@link ArrayList}.
 */

public class InMemoryPersistenceStrategy<E extends Data> implements PersistenceStrategy<E> {
    protected ArrayList<E> dataList = new ArrayList<>();
    private boolean initialized;

    public boolean isInitialized() {
        return initialized;
    }

    // Todo, remove check before adding. Done.

    @Override
    public boolean add(Collection<E> dataCollection) {
        boolean isAdded = false;
        for (E data : dataCollection) {
            dataList.add(data);
            isAdded = true;
        }
        return isAdded;
    }

    public void add(E data) {
        dataList.add(data);
    }

    public boolean hasData(E data) {
        return dataList.contains(data);
    }

    @Override
    public Collection<E> getData() {
        return new ArrayList<>(dataList);
    }

    @Override
    public void removeData(Collection<E> dataCollection) {
        dataList.removeAll(dataCollection);
    }

    @Override
    public void onInitialized() {
        initialized = true;
    }
}
