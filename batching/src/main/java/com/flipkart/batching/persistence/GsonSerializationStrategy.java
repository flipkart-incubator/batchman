package com.flipkart.batching.persistence;

import com.flipkart.batching.Batch;
import com.flipkart.batching.Data;
import com.flipkart.batching.DataCollection;
import com.flipkart.batching.exception.DeserializeException;
import com.flipkart.batching.exception.SerializeException;
import com.flipkart.batching.toolbox.RuntimeTypeAdapterFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of {@link SerializationStrategy}.
 *
 * @see SerializationStrategy
 * @see ByteArraySerializationStrategy
 */

public class GsonSerializationStrategy<E extends Data, T extends Batch> implements SerializationStrategy<E, T> {

    Set<Class<E>> dataTypes = new HashSet<>();
    Set<Class<T>> batchInfoTypes = new HashSet<>();
    private Gson gson;

    @Override
    public void registerDataType(Class<E> subClass) {
        dataTypes.add(subClass);
    }

    @Override
    public void registerBatch(Class<T> subClass) {
        batchInfoTypes.add(subClass);
    }

    @Override
    public void build() {
        RuntimeTypeAdapterFactory<E> dataAdapter = (RuntimeTypeAdapterFactory<E>) RuntimeTypeAdapterFactory.of(Data.class);
        for (Class<E> dataType : dataTypes) {
            dataAdapter.registerSubtype(dataType);
        }

        RuntimeTypeAdapterFactory<T> batchInfoAdapter = (RuntimeTypeAdapterFactory<T>) RuntimeTypeAdapterFactory.of(Batch.class);
        for (Class<T> batchInfoType : batchInfoTypes) {
            batchInfoAdapter.registerSubtype(batchInfoType);
        }

        RuntimeTypeAdapterFactory<Collection> collectionAdapter = RuntimeTypeAdapterFactory.of(Collection.class);
        collectionAdapter.registerSubtype(ArrayList.class);

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapterFactory(dataAdapter);
        gsonBuilder.registerTypeAdapterFactory(batchInfoAdapter);
        gsonBuilder.registerTypeAdapter(DataCollection.class, new DataCollection.Serializer());
        gsonBuilder.registerTypeAdapter(DataCollection.class, new DataCollection.DeSerializer());
        //gsonBuilder.registerTypeAdapterFactory(collectionAdapter);
        gson = gsonBuilder.create();

    }

    private void checkIfBuildCalled() {
        if (gson == null) {
            throw new IllegalStateException("The build() method was not called on " + getClass());
        }
    }

    @Override
    public byte[] serializeData(E data) throws SerializeException {
        checkIfBuildCalled();
        try {
            return gson.toJson(data, Data.class).getBytes();
        } catch (JsonParseException e) {
            throw new SerializeException(e);
        }
    }

    @Override
    public byte[] serializeCollection(Collection<E> data) throws SerializeException {
        checkIfBuildCalled();
        Type type = new TypeToken<Collection<Data>>() {
        }.getType();
        try {
            return gson.toJson(data, type).getBytes();
        } catch (JsonParseException e) {
            throw new SerializeException(e);
        }
    }

    @Override
    public byte[] serializeBatch(T batch) throws SerializeException {
        checkIfBuildCalled();
        try {
            return gson.toJson(batch, Batch.class).getBytes();
        } catch (JsonParseException e) {
            throw new SerializeException(e);
        }
    }

    @Override
    public E deserializeData(byte[] data) throws DeserializeException {
        checkIfBuildCalled();
        try {
            return (E) gson.fromJson(new String(data), Data.class);
        } catch (JsonParseException e) {
            throw new DeserializeException(e);
        }
    }

    @Override
    public Collection<E> deserializeCollection(byte[] data) throws DeserializeException {
        checkIfBuildCalled();

        Type type = new TypeToken<Collection<Data>>() {
        }.getType();
        try {
            return gson.fromJson(new String(data), type);
        } catch (JsonParseException e) {
            throw new DeserializeException(e);
        }
    }

    @Override
    public T deserializeBatch(byte[] data) throws DeserializeException {
        checkIfBuildCalled();
        try {
            return (T) gson.fromJson(new String(data), Batch.class);
        } catch (JsonParseException e) {
            throw new DeserializeException(e);
        }
    }
}
