package com.flipkart.persistence;

import com.flipkart.batching.BatchInfo;
import com.flipkart.data.Batch;
import com.flipkart.data.Data;
import com.flipkart.exception.DeserializeException;
import com.flipkart.exception.SerializeException;
import com.flipkart.toolbox.RuntimeTypeAdapterFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of {@link SerializationStrategy}.
 *
 * @see SerializationStrategy
 * @see ByteArraySerializationStrategy
 */

public class GsonSerializationStrategy implements SerializationStrategy {


    Set<Class<? extends Data>> dataTypes = new HashSet<>();
    Set<Class<? extends BatchInfo>> batchInfoTypes = new HashSet<>();
    private Gson gson;

    @Override
    public void registerDataType(Class<? extends Data> subClass) {
        dataTypes.add(subClass);
    }

    @Override
    public void registerBatchInfoType(Class<? extends BatchInfo> subClass) {
        batchInfoTypes.add(subClass);
    }


    @Override
    public void build() {
        RuntimeTypeAdapterFactory<Data> dataAdapter = RuntimeTypeAdapterFactory.of(Data.class);
        for (Class<? extends Data> dataType : dataTypes) {
            dataAdapter.registerSubtype(dataType);
        }

        RuntimeTypeAdapterFactory<BatchInfo> batchInfoAdapter = RuntimeTypeAdapterFactory.of(BatchInfo.class);
        for (Class<? extends BatchInfo> batchInfoType : batchInfoTypes) {
            batchInfoAdapter.registerSubtype(batchInfoType);
        }

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapterFactory(dataAdapter);
        gsonBuilder.registerTypeAdapterFactory(batchInfoAdapter);
        gson = gsonBuilder.create();

    }


    private void checkIfBuildCalled() {
        if (gson == null) {
            throw new IllegalStateException("The build() method was not called on " + getClass());
        }
    }

    @Override
    public byte[] serializeData(Data data) throws SerializeException {
        checkIfBuildCalled();
        return gson.toJson(data,Data.class).getBytes();
    }


    @Override
    public byte[] serializeCollection(Collection<Data> data) throws SerializeException {
        checkIfBuildCalled();
        return gson.toJson(data,Collection.class).getBytes();
    }

    @Override
    public byte[] serializeBatch(Batch batch) throws SerializeException {
        checkIfBuildCalled();
        return gson.toJson(batch,Batch.class).getBytes();
    }

    @Override
    public Data deserializeData(byte[] data) throws DeserializeException {
        checkIfBuildCalled();
        return gson.fromJson(new String(data), Data.class);
    }

    @Override
    public Collection<Data> deserializeCollection(byte[] data) throws DeserializeException {
        Type collectionType = new TypeToken<Collection<Data>>() {
        }.getType();
        return gson.fromJson(new String(data), collectionType);
    }

    @Override
    public Batch deserializeBatch(byte[] data) throws DeserializeException {
        checkIfBuildCalled();
        return gson.fromJson(new String(data), Batch.class);
    }

}
