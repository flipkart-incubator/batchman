package com.flipkart.batching.persistence;

import com.flipkart.batching.Data;
import com.flipkart.batching.data.Tag;
import com.flipkart.batching.data.TagData;

import java.util.Collection;
import java.util.Iterator;

/**
 * Tag Based Persistence Strategy is an implementation of {@link PersistenceStrategy}.
 * This strategy links the provide {@link Tag} with provided {@link PersistenceStrategy} and
 * persist {@link Data} objects depending on there {@link Tag}.
 */

public class TagBasedPersistenceStrategy<E extends Data> implements PersistenceStrategy<E> {
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
    public void add(Collection<E> dataCollection) {
        filterByTag(dataCollection);
        persistenceStrategy.add(dataCollection);
    }

    @Override
    public Collection<E> getData() {
        Collection<E> allData = persistenceStrategy.getData();
        filterByTag(allData);
        return allData;
    }

    @Override
    public void removeData(Collection<E> dataCollection) {
        filterByTag(dataCollection);
        persistenceStrategy.removeData(dataCollection);
    }

    @Override
    public void onInitialized() {
        persistenceStrategy.onInitialized();
    }

    /**
     * This method filters the provided collection of {@link Data} objects by {@link Tag}.
     *
     * @param allData collection of {@link Data} objects.
     */

    private void filterByTag(Collection<E> allData) {
        Iterator<E> iterator = allData.iterator();
        while (iterator.hasNext()) {
            TagData data = (TagData) iterator.next();
            if (!tag.equals(data.getTag())) {
                iterator.remove();
            }
        }
    }
}
