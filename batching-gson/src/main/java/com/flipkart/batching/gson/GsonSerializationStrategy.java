/*
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2017 Flipkart Internet Pvt. Ltd.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package com.flipkart.batching.gson;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.flipkart.batching.core.Batch;
import com.flipkart.batching.core.BatchImpl;
import com.flipkart.batching.core.Data;
import com.flipkart.batching.core.SerializationStrategy;
import com.flipkart.batching.core.batch.SizeBatch;
import com.flipkart.batching.core.batch.SizeTimeBatch;
import com.flipkart.batching.core.batch.TagBatch;
import com.flipkart.batching.core.batch.TimeBatch;
import com.flipkart.batching.core.data.EventData;
import com.flipkart.batching.core.data.TagData;
import com.flipkart.batching.gson.adapters.BatchImplTypeAdapter;
import com.flipkart.batching.gson.adapters.BatchingTypeAdapterFactory;
import com.flipkart.batching.gson.adapters.BatchingTypeAdapters;
import com.flipkart.batching.gson.adapters.batch.SizeBatchTypeAdapter;
import com.flipkart.batching.gson.adapters.batch.SizeTimeBatchTypeAdapter;
import com.flipkart.batching.gson.adapters.batch.TagBatchTypeAdapter;
import com.flipkart.batching.gson.adapters.batch.TimeBatchTypeAdapter;
import com.flipkart.batching.gson.adapters.data.EventDataTypeAdapter;
import com.flipkart.batching.gson.adapters.data.TagDataTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Implementation of {@link SerializationStrategy}.
 *
 * @see SerializationStrategy
 */
public class GsonSerializationStrategy<E extends Data, T extends Batch> implements SerializationStrategy<E, T> {

    private Gson gson;
    @Nullable
    private TypeAdapter<E> dataTypeAdapter;
    @Nullable
    private TypeAdapter<T> batchTypeAdapter;
    @Nullable
    private TypeAdapter<Collection<E>> collectionTypeAdapter;
    @Nullable
    private RuntimeTypeAdapterFactory<Data> runTimeDataTypeAdapter;
    @Nullable
    private RuntimeTypeAdapterFactory<Batch> runTimeBatchTypeAdapter;

    public GsonSerializationStrategy(@NonNull TypeAdapter<E> dataTypeAdapter, @NonNull TypeAdapter<T> batchTypeAdapter) {
        this.dataTypeAdapter = dataTypeAdapter;
        this.batchTypeAdapter = batchTypeAdapter;
    }

    public GsonSerializationStrategy() {
        this.dataTypeAdapter = null;
        this.batchTypeAdapter = null;
    }

    private TypeAdapter<Collection<E>> getCollectionTypeAdapter() {
        if (collectionTypeAdapter == null) {
            collectionTypeAdapter = new BatchingTypeAdapters.ListTypeAdapter<>(getDataTypeAdapter(), new ObjectConstructor<Collection<E>>() {
                @Override
                public Collection<E> construct() {
                    return new ArrayList<>();
                }
            });
        }
        return collectionTypeAdapter;
    }

    private TypeAdapter<T> getBatchTypeAdapter() {
        if (batchTypeAdapter == null) {
            batchTypeAdapter = (TypeAdapter<T>) gson.getAdapter(Batch.class);
        }
        return batchTypeAdapter;
    }

    private TypeAdapter<E> getDataTypeAdapter() {
        if (dataTypeAdapter == null) {
            dataTypeAdapter = (TypeAdapter<E>) gson.getAdapter(Data.class);
        }
        return dataTypeAdapter;
    }

    public void registerDataSubTypeAdapters(Class<? extends Data> subClass, TypeAdapter<? extends Data> typeAdapter) {
        getDataRuntimeTypeAdapter().registerSubtype(subClass, typeAdapter);
    }

    public void registerBatchSubTypeAdapters(Class<? extends Batch> subClass, TypeAdapter<? extends Batch> typeAdapter) {
        getBatchRuntimeTypeAdapter().registerSubtype(subClass, typeAdapter);
    }

    private RuntimeTypeAdapterFactory<Data> getDataRuntimeTypeAdapter() {
        if (runTimeDataTypeAdapter == null) {
            runTimeDataTypeAdapter = RuntimeTypeAdapterFactory.of(Data.class);
        }
        return runTimeDataTypeAdapter;
    }

    private RuntimeTypeAdapterFactory<Batch> getBatchRuntimeTypeAdapter() {
        if (runTimeBatchTypeAdapter == null) {
            runTimeBatchTypeAdapter = RuntimeTypeAdapterFactory.of(Batch.class);
        }
        return runTimeBatchTypeAdapter;
    }

    @Override
    public void build() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapterFactory(getDataRuntimeTypeAdapter());
        gsonBuilder.registerTypeAdapterFactory(getBatchRuntimeTypeAdapter());
        gsonBuilder.registerTypeAdapterFactory(new BatchingTypeAdapterFactory());
        registerDataSubTypeAdapters(EventData.class, new EventDataTypeAdapter());
        registerDataSubTypeAdapters(TagData.class, new TagDataTypeAdapter());

        gson = gsonBuilder.create();

        //Register Built in types
        registerBatchSubTypeAdapters(TagBatch.class, new TagBatchTypeAdapter<>(getDataTypeAdapter()));
        registerBatchSubTypeAdapters(SizeBatch.class, new SizeBatchTypeAdapter<>(getDataTypeAdapter()));
        registerBatchSubTypeAdapters(BatchImpl.class, new BatchImplTypeAdapter<>(getDataTypeAdapter()));
        registerBatchSubTypeAdapters(SizeTimeBatch.class, new SizeTimeBatchTypeAdapter<>(getDataTypeAdapter()));
        registerBatchSubTypeAdapters(TimeBatch.class, new TimeBatchTypeAdapter<>(getDataTypeAdapter()));
    }

    private void checkIfBuildCalled() {
        if (gson == null) {
            throw new IllegalStateException("The build() method was not called on " + getClass());
        }
    }

    @Override
    public byte[] serializeData(E data) throws IOException {
        checkIfBuildCalled();
        StringWriter stringWriter = new StringWriter();
        getDataTypeAdapter().toJson(stringWriter, data);
        return stringWriter.toString().getBytes();
    }

    @Override
    public byte[] serializeCollection(Collection<E> data) throws IOException {
        checkIfBuildCalled();
        StringWriter stringWriter = new StringWriter();
        getCollectionTypeAdapter().toJson(stringWriter, data);
        return stringWriter.toString().getBytes();
    }

    @Override
    public byte[] serializeBatch(T batch) throws IOException {
        checkIfBuildCalled();
        StringWriter stringWriter = new StringWriter();
        getBatchTypeAdapter().toJson(stringWriter, batch);
        return stringWriter.toString().getBytes();
    }

    @Override
    public E deserializeData(byte[] data) throws IOException {
        checkIfBuildCalled();
        return getDataTypeAdapter().read(new JsonReader(new StringReader(new String(data))));
    }

    @Override
    public Collection<E> deserializeCollection(byte[] data) throws IOException {
        checkIfBuildCalled();
        return getCollectionTypeAdapter().read(new JsonReader(new StringReader(new String(data))));
    }

    @Override
    public T deserializeBatch(byte[] data) throws IOException {
        checkIfBuildCalled();
        return getBatchTypeAdapter().read(new JsonReader(new StringReader(new String(data))));
    }
}