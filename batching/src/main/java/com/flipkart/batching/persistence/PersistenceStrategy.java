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

import java.util.Collection;

/**
 * Interface for PersistenceStrategy. A persistence strategy must implement this interface
 * and override all it's methods. Persistence strategy is responsible for persisting the.
 */
public interface PersistenceStrategy<E extends Data> {

    /**
     * This method tells the persistence strategy about the added {@link Collection} of {@link Data}
     * and persist it according to the provided implementation of persistenceStrategy.
     *
     * @param dataCollection collection of {@link Data} objects
     */
    boolean add(Collection<E> dataCollection);

    /**
     * This method returns {@link Collection} of persisted {@link Data} objects.
     *
     * @return collection of {@link Data} objects
     */
    Collection<E> getData();

    /**
     * This method returns {@link int} size of persisted {@link Data} objects.
     *
     * @return size of {@link int} objects
     */
    int getDataSize();

    /**
     * This method removes the provided {@link Collection} of {@link Data} objects from
     * the provided implementation of {@link PersistenceStrategy}.
     *
     * @param dataCollection collection of {@link Data} objects
     */
    void removeData(Collection<E> dataCollection);

    void onInitialized();
}
