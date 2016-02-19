package com.flipkart.persistence;

import com.flipkart.batching.BatchInfo;
import com.flipkart.data.Batch;
import com.flipkart.data.Data;
import com.flipkart.exception.DeserializeException;
import com.flipkart.exception.SerializeException;

import java.util.Collection;

/**
 * This is an interface for serialization strategy. An implementation of this class
 * must override all it's methods.
 *
 * @see ByteArraySerializationStrategy
 * @see GsonSerializationStrategy
 */

public interface SerializationStrategy {

    /**
     * Register sub classes of {@link Data}
     *
     * @param subClass The sub class of {@link Data}
     */
    void registerDataType(Class<? extends Data> subClass);

    /**
     * Register sub classes of {@link BatchInfo}
     *
     * @param subClass The sub class of {@link BatchInfo}
     */
    void registerBatchInfoType(Class<? extends BatchInfo> subClass);

    /**
     * After registering subtypes using {@link #registerDataType(Class)}} & {@link #registerBatchInfoType(Class)}, this method has to be called.
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

    byte[] serializeData(Data data) throws SerializeException;

    byte[] serializeCollection(Collection<Data> data) throws SerializeException;

    byte[] serializeBatch(Batch batch) throws SerializeException;

    /**
     * This method deserialize the provided byte array of data.
     *
     * @param data byte[] type data
     * @return {@link Object} type data
     * @throws DeserializeException
     */

    Data deserializeData(byte[] data) throws DeserializeException;

    Collection<Data> deserializeCollection(byte[] data) throws DeserializeException;

    Batch deserializeBatch(byte[] data) throws DeserializeException;

}
