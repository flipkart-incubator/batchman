package com.flipkart.persistence;

import com.flipkart.data.Data;
import com.flipkart.data.Tag;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by kushal.sharma on 11/02/16.
 */
public class TagBasedPersistenceStrategy implements PersistenceStrategy {
    private final PersistenceStrategy persistenceStrategy;
    private final Tag tag;

    public TagBasedPersistenceStrategy(Tag tag, PersistenceStrategy persistenceStrategy) {
        this.tag = tag;
        this.persistenceStrategy = persistenceStrategy;
        if (tag == null) {
            throw new IllegalArgumentException("Tag cannot be null");
        }
        if (persistenceStrategy == null) {
            throw new IllegalArgumentException("PersistenceStrategy cannot be null");
        }
    }

    @Override
    public void add(Collection<Data> allData) {
        filterByTag(allData);
        persistenceStrategy.add(allData);
    }


    @Override
    public Collection<Data> getData() {
        Collection<Data> allData = persistenceStrategy.getData();
        filterByTag(allData);
        return allData;
    }

    @Override
    public void removeData(Collection<Data> allData) {
        filterByTag(allData);
        persistenceStrategy.removeData(allData);
    }

    private void filterByTag(Collection<Data> allData) {
        Iterator<Data> iterator = allData.iterator();
        while (iterator.hasNext()) {
            Data data = iterator.next();
            if (!tag.equals(data.getTag())) {
                iterator.remove();
            }
        }
    }
}
