/*
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2017 Flipkart Internet Pvt. Ltd.
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

package com.flipkart.batching;

import android.os.Handler;

import com.flipkart.batching.core.Batch;
import com.flipkart.batching.core.Data;
import com.flipkart.batching.core.SerializationStrategy;

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
     * This method takes {@link Data} type {@link Collection} and a boolean as parameter and notifies the provided
     * {@link BatchingStrategy} about the added data.
     *
     * @param dataCollection collection of {@link Data}
     * @param forced whether to forcefully trigger the event call
     */
    void addToBatch(Collection<E> dataCollection, boolean forced);

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
