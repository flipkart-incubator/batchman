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

import com.flipkart.batching_core.Data;

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
