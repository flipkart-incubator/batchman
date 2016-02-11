package com.flipkart.persistence;

import com.flipkart.data.Data;
import com.flipkart.exception.DeserializeException;
import com.flipkart.exception.SerializeException;

public interface SerializationStrategy {

    byte[] serialize(Data data) throws SerializeException;

    Object deserialize(byte[] data) throws DeserializeException;

}
