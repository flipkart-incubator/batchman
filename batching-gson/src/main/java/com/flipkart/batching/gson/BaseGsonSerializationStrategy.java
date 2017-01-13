package com.flipkart.batching.gson;

import com.flipkart.batching.core.Batch;
import com.flipkart.batching.core.Data;
import com.flipkart.batching.core.SerializationStrategy;
import com.google.gson.TypeAdapter;

public interface BaseGsonSerializationStrategy<E extends Data, T extends Batch> extends SerializationStrategy<E, T> {
    void registerDataSubTypeAdapters(Class<? extends Data> subClass, TypeAdapter<? extends Data> typeAdapter);

    void registerBatchSubTypeAdapters(Class<? extends Batch> subClass, TypeAdapter<? extends Batch> typeAdapter);
}