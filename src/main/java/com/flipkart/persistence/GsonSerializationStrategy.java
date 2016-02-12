package com.flipkart.persistence;

import com.flipkart.data.Data;
import com.flipkart.data.EventData;
import com.flipkart.exception.DeserializeException;
import com.flipkart.exception.SerializeException;
import com.google.gson.Gson;

/**
 * Implementation of {@link SerializationStrategy}.
 *
 * @see SerializationStrategy
 * @see ByteArraySerializationStrategy
 */

public class GsonSerializationStrategy implements SerializationStrategy {

    @Override
    public byte[] serialize(Data data) throws SerializeException {
        Gson gson = new Gson();
        return gson.toJson(data).getBytes();
    }

    @Override
    public Object deserialize(byte[] data) throws DeserializeException {
        Gson gson = new Gson();
        return gson.fromJson(new String(data), EventData.class);
    }

}
