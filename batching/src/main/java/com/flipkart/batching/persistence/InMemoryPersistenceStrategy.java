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
