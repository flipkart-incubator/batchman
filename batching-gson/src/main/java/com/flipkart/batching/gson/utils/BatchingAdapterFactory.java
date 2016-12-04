package com.flipkart.batching.gson.utils;

import com.flipkart.batching.core.DataCollection;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class BatchingAdapterFactory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<?> rawClazz = type.getRawType();
        if (rawClazz == DataCollection.class) {
            Type parameters = type.getType();
            if (parameters instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) parameters;
                Type[] parametersType = parameterizedType.getActualTypeArguments();
                return new DataCollectionTypeAdapter(gson, parametersType[0]);
            }
        }
        return null;
    }
}