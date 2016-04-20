package com.flipkart.batching.persistence;

import com.flipkart.batching.Batch;
import com.flipkart.batching.Data;
import com.flipkart.batching.tape.FileObjectQueue;

import java.io.IOException;
import java.io.OutputStream;

public class DataObjectConverter<E extends Data> implements FileObjectQueue.Converter<E> {
    private SerializationStrategy<E, ? extends Batch> serializationStrategy;

    public DataObjectConverter(SerializationStrategy<E, ? extends Batch> serializationStrategy) {
        this.serializationStrategy = serializationStrategy;
    }

    @Override
    public E from(byte[] bytes) throws IOException {
        return serializationStrategy.deserializeData(bytes);

    }

    @Override
    public void toStream(E data, OutputStream bytes) throws IOException {
        bytes.write(serializationStrategy.serializeData(data));
    }
}