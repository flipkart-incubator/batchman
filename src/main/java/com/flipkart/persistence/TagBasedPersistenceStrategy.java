package com.flipkart.persistence;

import com.flipkart.data.Data;
import com.flipkart.data.Tag;

import java.util.Collection;
import java.util.Iterator;

/**
 * Tag Based Persistence Strategy is an implementation of {@link PersistenceStrategy}.
 * This strategy links the provide {@link Tag} with provided {@link PersistenceStrategy} and
 * persist {@link Data} objects depending on there {@link Tag}.
 */

public class TagBasedPersistenceStrategy implements PersistenceStrategy {
    private final PersistenceStrategy persistenceStrategy;
    private final Tag tag;

    public TagBasedPersistenceStrategy(Tag tag, PersistenceStrategy persistenceStrategy) {
        if (tag == null) {
            throw new IllegalArgumentException("Tag cannot be null");
        } else {
            this.tag = tag;
        }

        if (persistenceStrategy == null) {
            throw new IllegalArgumentException("PersistenceStrategy cannot be null");
        } else {
            this.persistenceStrategy = persistenceStrategy;
        }
    }

    @Override
    public void add(Collection<Data> dataCollection) {
        filterByTag(dataCollection);
        persistenceStrategy.add(dataCollection);
    }

    @Override
    public Collection<Data> getData() {
        Collection<Data> allData = persistenceStrategy.getData();
        filterByTag(allData);
        return allData;
    }

    @Override
    public void removeData(Collection<Data> dataCollection) {
        filterByTag(dataCollection);
        persistenceStrategy.removeData(dataCollection);
    }

    /**
     * This method filters the provided collection of {@link Data} objects by {@link Tag}.
     *
     * @param allData collection of {@link Data} objects.
     */

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
