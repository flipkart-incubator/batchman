package com.flipkart.persistence;

import android.content.Context;

import com.flipkart.batching.BatchController;
import com.flipkart.data.Data;

import java.util.ArrayList;
import java.util.Collection;

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

}
