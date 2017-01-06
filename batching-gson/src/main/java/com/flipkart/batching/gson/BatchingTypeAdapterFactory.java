package com.flipkart.batching.gson;

import com.flipkart.batching.core.DataCollection;
import com.flipkart.batching.core.batch.SizeBatch;
import com.flipkart.batching.core.batch.SizeTimeBatch;
import com.flipkart.batching.core.batch.TagBatch;
import com.flipkart.batching.core.batch.TimeBatch;
import com.flipkart.batching.core.data.EventData;
import com.flipkart.batching.core.data.Tag;
import com.flipkart.batching.core.data.TagData;
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

public class BatchingTypeAdapterFactory implements TypeAdapterFactory {

    private TypeAdapter<Tag> tagTypeAdapter;

    public TypeAdapter<Tag> getTagTypeAdapter() {
        if (null == tagTypeAdapter) {
            tagTypeAdapter = new TagTypeAdapter();
        }
        return tagTypeAdapter;
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<? super T> clazz = type.getRawType();

        if (clazz == DataCollection.class) {
            java.lang.reflect.Type parameters = type.getType();
            TypeAdapter typeAdapter;
            if (parameters instanceof java.lang.reflect.ParameterizedType) {
                java.lang.reflect.ParameterizedType parameterizedType = (java.lang.reflect.ParameterizedType) parameters;
                java.lang.reflect.Type[] parametersType = parameterizedType.getActualTypeArguments();
                typeAdapter = gson.getAdapter(TypeToken.get(parametersType[0]));
            } else {
                TypeToken objectToken = TypeToken.get(Object.class);
                typeAdapter = gson.getAdapter(objectToken);
            }
            return (TypeAdapter<T>) new DataCollectionTypeAdapter<>(typeAdapter);
        }

        if (clazz == SizeBatch.class) {
            java.lang.reflect.Type parameters = type.getType();
            TypeAdapter typeAdapter;
            if (parameters instanceof java.lang.reflect.ParameterizedType) {
                java.lang.reflect.ParameterizedType parameterizedType = (java.lang.reflect.ParameterizedType) parameters;
                java.lang.reflect.Type[] parametersType = parameterizedType.getActualTypeArguments();
                typeAdapter = gson.getAdapter(TypeToken.get(parametersType[0]));
            } else {
                TypeToken objectToken = TypeToken.get(Object.class);
                typeAdapter = gson.getAdapter(objectToken);
            }
            return (TypeAdapter<T>) new SizeBatchTypeAdapter(typeAdapter);
        }

        if (clazz == SizeTimeBatch.class) {
            java.lang.reflect.Type parameters = type.getType();
            TypeAdapter typeAdapter;
            if (parameters instanceof java.lang.reflect.ParameterizedType) {
                java.lang.reflect.ParameterizedType parameterizedType = (java.lang.reflect.ParameterizedType) parameters;
                java.lang.reflect.Type[] parametersType = parameterizedType.getActualTypeArguments();
                typeAdapter = gson.getAdapter(TypeToken.get(parametersType[0]));
            } else {
                TypeToken objectToken = TypeToken.get(Object.class);
                typeAdapter = gson.getAdapter(objectToken);
            }
            return (TypeAdapter<T>) new SizeTimeBatchTypeAdapter(typeAdapter);
        }

        if (clazz == TimeBatch.class) {
            java.lang.reflect.Type parameters = type.getType();
            TypeAdapter typeAdapter;
            if (parameters instanceof java.lang.reflect.ParameterizedType) {
                java.lang.reflect.ParameterizedType parameterizedType = (java.lang.reflect.ParameterizedType) parameters;
                java.lang.reflect.Type[] parametersType = parameterizedType.getActualTypeArguments();
                typeAdapter = gson.getAdapter(TypeToken.get(parametersType[0]));
            } else {
                TypeToken objectToken = TypeToken.get(Object.class);
                typeAdapter = gson.getAdapter(objectToken);
            }
            return (TypeAdapter<T>) new TimeBatchTypeAdapter(typeAdapter);
        }

        if (clazz == TagBatch.class) {
            java.lang.reflect.Type parameters = type.getType();
            TypeAdapter typeAdapter;
            if (parameters instanceof java.lang.reflect.ParameterizedType) {
                java.lang.reflect.ParameterizedType parameterizedType = (java.lang.reflect.ParameterizedType) parameters;
                java.lang.reflect.Type[] parametersType = parameterizedType.getActualTypeArguments();
                typeAdapter = gson.getAdapter(TypeToken.get(parametersType[0]));
            } else {
                TypeToken objectToken = TypeToken.get(Object.class);
                typeAdapter = gson.getAdapter(objectToken);
            }
            return (TypeAdapter<T>) new TagBatchTypeAdapter(this, typeAdapter);
        }

        if (clazz == Tag.class) {
            return (TypeAdapter<T>) getTagTypeAdapter();
        }

        if (clazz == TagData.class) {
            return (TypeAdapter<T>) new TagDataTypeAdapter(this);
        }

        if (clazz == EventData.class) {
            return (TypeAdapter<T>) new EventDataTypeAdapter();
        }

        return null;
    }
}