package com.flipkart.persistence;

import com.flipkart.data.Data;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This is a simple implementation of {@link PersistenceStrategy}.
 * This strategy persist data in an InMemory {@link ArrayList}.
 */

public class InMemoryPersistenceStrategy implements PersistenceStrategy {
    private ArrayList<Data> dataList = new ArrayList<>();

    @Override
    public void add(Collection<Data> dataCollection) {
        for (Data data : dataCollection) {
            if (!dataList.contains(data)) dataList.add(data);
        }
    }

    @Override
    public Collection<Data> getData() {
        return new ArrayList<>(dataList);
    }

    @Override
    public void removeData(Collection<Data> dataCollection) {
        dataList.removeAll(dataCollection);
    }

    @Override
    public void onInitialized() {

    }

}
