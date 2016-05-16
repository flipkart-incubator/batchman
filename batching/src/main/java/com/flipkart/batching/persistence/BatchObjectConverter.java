package com.flipkart.batching.persistence;

import com.flipkart.batching.Batch;
import com.flipkart.batching.Data;
import com.flipkart.batching.tape.FileObjectQueue;

import java.io.IOException;
import java.io.OutputStream;

public class BatchObjectConverter<E extends Data, T extends Batch<E>> implements FileObjectQueue.Converter<T> {
    private SerializationStrategy<E, T> serializationStrategy;

    public BatchObjectConverter(SerializationStrategy<E, T> serializationStrategy) {
        this.serializationStrategy = serializationStrategy;
    }

    @Override
    public T from(byte[] bytes) throws IOException {
        return serializationStrategy.deserializeBatch(bytes);
    }

    @Override
    public void toStream(T batch, OutputStream bytes) throws IOException {
        bytes.write(serializationStrategy.serializeBatch(batch));
    }
}