package com.flipkart.batching.gson;

import com.flipkart.batching.core.BatchImpl;
import com.flipkart.batching.core.DataCollection;
import com.flipkart.batching.core.batch.SizeBatch;
import com.flipkart.batching.core.batch.SizeTimeBatch;
import com.flipkart.batching.core.batch.TagBatch;
import com.flipkart.batching.core.batch.TimeBatch;
import com.flipkart.batching.core.data.EventData;
import com.flipkart.batching.core.data.Tag;
import com.flipkart.batching.core.data.TagData;
import com.flipkart.batching.gson.adapters.BatchImplTypeAdapter;
import com.flipkart.batching.gson.adapters.DataCollectionTypeAdapter;
import com.flipkart.batching.gson.adapters.batch.SizeBatchTypeAdapter;
import com.flipkart.batching.gson.adapters.batch.SizeTimeBatchTypeAdapter;
import com.flipkart.batching.gson.adapters.batch.TagBatchTypeAdapter;
import com.flipkart.batching.gson.adapters.batch.TimeBatchTypeAdapter;
import com.flipkart.batching.gson.adapters.data.EventDataTypeAdapter;
import com.flipkart.batching.gson.adapters.data.TagDataTypeAdapter;
import com.flipkart.batching.gson.adapters.data.TagTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class BatchingTypeAdapterFactory implements TypeAdapterFactory {

    private TypeAdapter<Tag> tagTypeAdapter;

    private static TypeAdapter getParameterizedTypeAdapter(Gson gson, Type parameters) {
        if (parameters instanceof java.lang.reflect.ParameterizedType) {
            java.lang.reflect.ParameterizedType parameterizedType = (java.lang.reflect.ParameterizedType) parameters;
            java.lang.reflect.Type[] parametersType = parameterizedType.getActualTypeArguments();
            return gson.getAdapter(TypeToken.get(parametersType[0]));
        } else {
            TypeToken objectToken = TypeToken.get(Object.class);
            return gson.getAdapter(objectToken);
        }
    }

    public TypeAdapter<Tag> getTagTypeAdapter() {
        if (null == tagTypeAdapter) {
            tagTypeAdapter = new TagTypeAdapter();
        }
        return tagTypeAdapter;
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<? super T> clazz = type.getRawType();

        if (clazz.isAssignableFrom(DataCollection.class)) {
            java.lang.reflect.Type parameters = type.getType();
            return (TypeAdapter<T>) new DataCollectionTypeAdapter<>(getParameterizedTypeAdapter(gson, parameters));
        }

        if (clazz.isAssignableFrom(SizeBatch.class)) {
            java.lang.reflect.Type parameters = type.getType();
            return (TypeAdapter<T>) new SizeBatchTypeAdapter(getParameterizedTypeAdapter(gson, parameters));
        }

        if (clazz.isAssignableFrom(SizeTimeBatch.class)) {
            java.lang.reflect.Type parameters = type.getType();
            return (TypeAdapter<T>) new SizeTimeBatchTypeAdapter(getParameterizedTypeAdapter(gson, parameters));
        }

        if (clazz.isAssignableFrom(TimeBatch.class)) {
            java.lang.reflect.Type parameters = type.getType();
            return (TypeAdapter<T>) new TimeBatchTypeAdapter(getParameterizedTypeAdapter(gson, parameters));
        }

        if (clazz.isAssignableFrom(TagBatch.class)) {
            java.lang.reflect.Type parameters = type.getType();
            return (TypeAdapter<T>) new TagBatchTypeAdapter(this, getParameterizedTypeAdapter(gson, parameters));
        }

        if (clazz.isAssignableFrom(BatchImpl.class)) {
            java.lang.reflect.Type parameters = type.getType();
            return (TypeAdapter<T>) new BatchImplTypeAdapter(getParameterizedTypeAdapter(gson, parameters));
        }

        if (clazz.isAssignableFrom(Tag.class)) {
            return (TypeAdapter<T>) getTagTypeAdapter();
        }

        if (clazz.isAssignableFrom(TagData.class)) {
            return (TypeAdapter<T>) new TagDataTypeAdapter(this);
        }

        if (clazz.isAssignableFrom(EventData.class)) {
            return (TypeAdapter<T>) new EventDataTypeAdapter();
        }

        return null;
    }
}