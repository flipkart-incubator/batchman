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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * A queue for objects that are not serious enough to be written to disk.  Objects in this queue
 * are kept in memory and will not be serialized.
 *
 * @param <T> The type of elements in the queue.
 */
public class InMemoryObjectQueue<T> implements ObjectQueue<T> {
    private final Queue<T> tasks;
    private Listener<T> listener;

    @SuppressWarnings("unchecked")
    public InMemoryObjectQueue() {
        tasks = (Queue<T>) new LinkedList();
    }

    @Override
    public void add(T entry) {
        tasks.add(entry);
        if (listener != null) listener.onAdd(this, entry);
    }

    @Override
    public T peek() {
        return tasks.peek();
    }

    @Override
    public int size() {
        return tasks.size();
    }

    @Override
    public void remove() {
        tasks.remove();
        if (listener != null) listener.onRemove(this);
    }

    @Override
    public void remove(int n) {
        for (int i = 0; i < n; i++) {
            remove();
        }
    }

    @Override
    public void close() {
        tasks.clear();
    }

    @Override
    public void setListener(Listener<T> listener) {
        if (listener != null) {
            for (T task : tasks) {
                listener.onAdd(this, task);
            }
        }
        this.listener = listener;
    }

    @Override
    public List<T> peek(final int max) {
        return new ArrayList<>(tasks);
    }
}