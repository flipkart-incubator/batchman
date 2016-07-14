/*
 * Copyright 2016 Flipkart Internet Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    private final PersistenceStrategy<E> persistenceStrategy;
    private final Tag tag;

    public TagBasedPersistenceStrategy(Tag tag, PersistenceStrategy<E> persistenceStrategy) {
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
    public boolean add(Collection<E> dataCollection) {
        filterByTag(dataCollection);
        return persistenceStrategy.add(dataCollection);
    }

    @Override
    public Collection<E> getData() {
        Collection<E> allData = persistenceStrategy.getData();
        filterByTag(allData);
        return allData;
    }

    @Override
    public int getDataSize() {
        return persistenceStrategy.getDataSize();
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
            if (null == data || !tag.equals(data.getTag())) {
                iterator.remove();
            }
        }
    }
}
