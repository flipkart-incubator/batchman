/*
 * Copyright 2012 Square, Inc.
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
package com.flipkart.batching.tape;

import java.io.IOException;
import java.util.Collection;

/**
 * A queue of objects.
 *
 * @param <T> The type of queue for the elements.
 */
public interface ObjectQueue<T> {

    /**
     * Returns the number of entries in the queue.
     */
    int size();

    /**
     * Enqueues an entry that can be processed at any time.
     */
    void add(T entry) throws IOException;

    /**
     * Returns the head of the queue, or {@code null} if the queue is empty. Does not modify the
     * queue.
     */
    T peek() throws IOException;

    /**
     * Returns the head of the queue, or {@code null} if the queue is empty. Does not modify the
     * queue.
     */
    Collection<T> peek(int max) throws IOException;

    /**
     * Removes the head of the queue.
     */
    void remove() throws IOException;

    /**
     * Removes n items from the head of the queue.
     */
    void remove(int size) throws IOException;

    /**
     * Closes the underlying queue file
     */
    void close() throws IOException;

    /**
     * Sets a listener on this queue. Invokes {@link Listener#onAdd} once for each entry that's
     * already in the queue. If an error occurs while reading the data, the listener will not receive
     * further notifications.
     */
    void setListener(Listener<T> listener);

    /**
     * Listens for changes to the queue.
     *
     * @param <T> The type of elements in the queue.
     */
    interface Listener<T> {

        /**
         * Called after an entry is added.
         */
        void onAdd(ObjectQueue<T> queue, T entry);

        /**
         * Called after an entry is removed.
         */
        void onRemove(ObjectQueue<T> queue);
    }
}
