package com.flipkart.batching.gson;

import com.flipkart.batching.core.DataCollection;
import com.flipkart.batching.gson.adapters.DataCollectionTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

public class BatchingTypeAdapterFactory implements TypeAdapterFactory {
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

        return null;
    }
}