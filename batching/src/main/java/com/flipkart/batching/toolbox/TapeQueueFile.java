package com.flipkart.batching.toolbox;

import com.flipkart.batching.Data;
import com.flipkart.batching.exception.DeserializeException;
import com.flipkart.batching.exception.SerializeException;
import com.flipkart.batching.persistence.SerializationStrategy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by kushal.sharma on 06/04/16.
 */

public class TapeQueueFile<E extends Data> implements IQueueFile {
    QueueFile queueFile;
    SerializationStrategy serializationStrategy;

    public TapeQueueFile(File file, SerializationStrategy serializationStrategy) throws IOException {
        queueFile = new QueueFile(file);
        this.serializationStrategy = serializationStrategy;
    }


    @Override
    public void add(Object data) throws SerializeException, IOException {
        queueFile.add(serializationStrategy.serializeData((E) data));
    }

    @Override
    public void remove() throws IOException {
        queueFile.remove();
    }

    @Override
    public E peek() throws IOException, DeserializeException {
        return (E) serializationStrategy.deserializeData(queueFile.peek());
    }

    @Override
    public int size() {
        return queueFile.size();
    }

    @Override
    public void close() throws IOException {
        queueFile.close();
    }

    @Override
    public Collection<E> peek(final int size) {
        final ArrayList<E> dataList = new ArrayList<>(size);
        try {
            queueFile.forEach(new QueueFile.ElementVisitor() {
                int count = 0;

                @Override
                public boolean read(InputStream in, int length) throws IOException {
                    byte[] data = new byte[length];
                    in.read(data, 0, length);
                    try {
                        dataList.add((E) serializationStrategy.deserializeData(data));
                    } catch (DeserializeException e) {
                        e.printStackTrace();
                    }
                    return ++count < size;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataList;
    }
}
