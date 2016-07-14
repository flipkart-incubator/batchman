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
