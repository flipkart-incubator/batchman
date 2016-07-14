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

import com.flipkart.batching.Batch;
import com.flipkart.batching.Data;
import com.flipkart.batching.exception.DeserializeException;
import com.flipkart.batching.exception.SerializeException;

import java.util.Collection;

/**
 * This is an interface for serialization strategy. An implementation of this class
 * must override all it's methods.
 *
 * @see GsonSerializationStrategy
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
