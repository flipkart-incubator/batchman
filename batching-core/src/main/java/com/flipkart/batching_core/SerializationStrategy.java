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

package com.flipkart.batching_core;

import com.flipkart.batching_core.exception.DeserializeException;
import com.flipkart.batching_core.exception.SerializeException;

import java.util.Collection;

/**
 * This is an interface for serialization strategy. An implementation of this class
 * must override all it's methods.
 */
public interface SerializationStrategy<E extends Data, T extends Batch> {

    /**
     * Register sub classes of {@link Data}
     *
     * @param subClass The sub class of {@link Data}
     */
    void registerDataType(Class<E> subClass);

    void registerBatch(Class<T> subClass);

    /**
     * After registering subtypes using {@link #registerDataType(Class)}} & {@link #registerBatch(Class)}, this method has to be called.
     * After build is called, no more register calls can happen.
     */
    void build();

    /**
     * This method serialize the provided {@link Data} object.
     *
     * @param data {@link Data} object to be serialized
     * @return byte array
     * @throws SerializeException
     */
    byte[] serializeData(E data) throws SerializeException;

    byte[] serializeCollection(Collection<E> data) throws SerializeException;

    byte[] serializeBatch(T batch) throws SerializeException;

    /**
     * This method deserialize the provided byte array of data.
     *
     * @param data byte[] type data
     * @return {@link Object} type data
     * @throws DeserializeException
     */
    E deserializeData(byte[] data) throws DeserializeException;

    Collection<E> deserializeCollection(byte[] data) throws DeserializeException;

    T deserializeBatch(byte[] data) throws DeserializeException;
}