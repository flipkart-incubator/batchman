/*
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2016 Flipkart Internet Pvt. Ltd.
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
