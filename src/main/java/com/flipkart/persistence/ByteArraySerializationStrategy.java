package com.flipkart.persistence;

import com.flipkart.batching.BatchInfo;
import com.flipkart.data.Batch;
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
import java.util.Collection;

/**
 * Implementation of {@link SerializationStrategy}.
 *
 * @see SerializationStrategy
 * @see GsonSerializationStrategy
 */

public class ByteArraySerializationStrategy implements SerializationStrategy {

    @Override
    public void registerDataType(Class<? extends Data> subClass) {

    }

    @Override
    public void registerBatchInfoType(Class<? extends BatchInfo> subClass) {

    }

    @Override
    public void build() {

    }

    @Override
    public byte[] serializeData(Data data) throws SerializeException {
        return getBytes(data);
    }

    private byte[] getBytes(Object data) throws SerializeException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(data);
            out.close();
        } catch (IOException e) {
            throw new SerializeException(e);
        }

        return bos.toByteArray();
    }

    @Override
    public byte[] serializeCollection(Collection<Data> data) throws SerializeException {
        return getBytes(data);
    }

    @Override
    public byte[] serializeBatch(Batch info) throws SerializeException {
        return getBytes(info);
    }

    /**
     * @param data
     * @return
     * @throws DeserializeException
     */
    @Override
    public Data deserializeData(byte[] data) throws DeserializeException {
        return (Data) getObject(data);
    }

    private Object getObject(byte[] data) throws DeserializeException {
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

    @Override
    public Collection<Data> deserializeCollection(byte[] data) throws DeserializeException {
        return (Collection<Data>) getObject(data);
    }

    @Override
    public Batch deserializeBatch(byte[] data) throws DeserializeException {
        return (Batch) getObject(data);
    }
}
