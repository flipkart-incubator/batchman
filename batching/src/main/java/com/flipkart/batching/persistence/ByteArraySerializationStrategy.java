package com.flipkart.batching.persistence;

import com.flipkart.batching.Batch;
import com.flipkart.batching.Data;
import com.flipkart.batching.exception.DeserializeException;
import com.flipkart.batching.exception.SerializeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Collection;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of {@link SerializationStrategy}.
 *
 * @see SerializationStrategy
 * @see GsonSerializationStrategy
 */
public class ByteArraySerializationStrategy<E extends Data, T extends Batch> implements SerializationStrategy<E, T> {

    Logger log = LoggerFactory.getLogger(ByteArraySerializationStrategy.class);

    @Override
    public void registerDataType(Class subClass) {

    }

    @Override
    public void registerBatch(Class subClass) {

    }

    @Override
    public void build() {

    }

    @Override
    public byte[] serializeData(E data) throws SerializeException {
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
            if (log.isErrorEnabled()) {
                log.error(e.getLocalizedMessage());
            }
            throw new SerializeException(e);
        }

        return bos.toByteArray();
    }

    @Override
    public byte[] serializeCollection(Collection<E> data) throws SerializeException {
        return getBytes(data);
    }

    @Override
    public byte[] serializeBatch(T info) throws SerializeException {
        return getBytes(info);
    }

    /**
     * @param data
     * @return
     * @throws DeserializeException
     */
    @Override
    public E deserializeData(byte[] data) throws DeserializeException {
        return (E) getObject(data);
    }

    private Object getObject(byte[] data) throws DeserializeException {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInput in;
        try {
            in = new ObjectInputStream(bis);
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getLocalizedMessage());
            }
            throw new DeserializeException(e);
        }

        try {
            return in.readObject();
        } catch (ClassNotFoundException | IOException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getLocalizedMessage());
            }
            throw new DeserializeException(e);
        }
    }

    @Override
    public Collection<E> deserializeCollection(byte[] data) throws DeserializeException {
        return (Collection<E>) getObject(data);
    }

    @Override
    public T deserializeBatch(byte[] data) throws DeserializeException {
        return (T) getObject(data);
    }
}
