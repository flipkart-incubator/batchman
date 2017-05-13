package com.flipkart.batching.flatbuffer;

import com.flipkart.batching.core.*;
import com.flipkart.batching.core.Batch;
import com.flipkart.batching.core.Data;

import java.io.IOException;
import java.util.Collection;

public class FlatBufSerializationStrategy implements SerializationStrategy {
    @Override
    public void build() {

    }

    @Override
    public byte[] serializeData(Data data) throws IOException {
        return new byte[0];
    }

    @Override
    public byte[] serializeCollection(Collection collection) throws IOException {
        return new byte[0];
    }

    @Override
    public byte[] serializeBatch(Batch batch) throws IOException {
        return new byte[0];
    }

    @Override
    public Data deserializeData(byte[] bytes) throws IOException {
        return null;
    }

    @Override
    public Collection deserializeCollection(byte[] bytes) throws IOException {
        return null;
    }

    @Override
    public Batch deserializeBatch(byte[] bytes) throws IOException {
        return null;
    }
}