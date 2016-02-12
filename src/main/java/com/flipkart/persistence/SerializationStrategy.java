package com.flipkart.persistence;

import com.flipkart.data.Data;
import com.flipkart.exception.DeserializeException;
import com.flipkart.exception.SerializeException;

/**
 * This is an interface for serialization strategy. An implementation of this class
 * must override all it's methods.
 *
 * @see ByteArraySerializationStrategy
 * @see GsonSerializationStrategy
 */

public interface SerializationStrategy {

    /**
     * This method serialize the provided {@link Data} object.
     *
     * @param data {@link Data} object to be serialized
     * @return byte array
     * @throws SerializeException
     */

    byte[] serialize(Data data) throws SerializeException;

    /**
     * This method deserialize the provided byte array of data.
     *
     * @param data byte[] type data
     * @return {@link Object} type data
     * @throws DeserializeException
     */

    Object deserialize(byte[] data) throws DeserializeException;

}
