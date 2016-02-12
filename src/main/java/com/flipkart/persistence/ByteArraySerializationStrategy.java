package com.flipkart.persistence;

import com.flipkart.data.Data;
import com.flipkart.exception.DeserializeException;
import com.flipkart.exception.SerializeException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

/**
 * Implementation of {@link SerializationStrategy}.
 *
 * @see SerializationStrategy
 * @see GsonSerializationStrategy
 */

public class ByteArraySerializationStrategy implements SerializationStrategy {

    @Override
    public byte[] serialize(Data data) throws SerializeException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(data);
        } catch (IOException e) {
            throw new SerializeException(e);
        }

        return bos.toByteArray();
    }

    /**
     * @param data
     * @return
     * @throws DeserializeException
     */
    @Override
    public Object deserialize(byte[] data) throws DeserializeException {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInput in;
        try {
            in = new ObjectInputStream(bis);
        } catch (IOException e) {
            throw new DeserializeException(e);
        }

        try {
            return in.readObject();
        } catch (ClassNotFoundException | IOException e) {
            throw new DeserializeException(e);
        }
    }
}
