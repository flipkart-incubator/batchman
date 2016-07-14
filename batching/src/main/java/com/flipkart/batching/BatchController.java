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

package com.flipkart.batching;

import android.os.Handler;

import com.flipkart.batching.persistence.SerializationStrategy;

import java.util.Collection;

/**
 * Interface class for BatchController. An implementation of BatchController must
 * implement this interface and override all it's methods.
 */

public interface BatchController<E extends Data, T extends Batch<E>> {
    /**
     * This method takes {@link Data} type {@link Collection} as parameter and notifies the provided
     * {@link BatchingStrategy} about the added data.
     *
     * @param dataCollection collection of {@link Data}
     */

    void addToBatch(Collection<E> dataCollection);

    /**
     * This method returns the initialized {@link Handler}.
     *
     * @return handler
     */

    Handler getHandler();

    /**
     * This method returns the initialized {@link SerializationStrategy}.
     *
     * @return serializationStrategy
     */

    SerializationStrategy<E, T> getSerializationStrategy();

    void flush(boolean forced);
}
