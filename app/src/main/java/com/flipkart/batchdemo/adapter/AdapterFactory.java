package com.flipkart.batchdemo.adapter;

import com.flipkart.batchdemo.CustomTagData;
import com.flipkart.batchdemo.EventTag;
import com.flipkart.batching.gson.BatchingTypeAdapterFactory;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

public class AdapterFactory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<? super T> rawType = type.getRawType();
        if (rawType == CustomTagData.class) {
            return (TypeAdapter<T>) new CustomTagDataAdapter(gson, new BatchingTypeAdapterFactory());
        }

        if (rawType == EventTag.class) {
            return (TypeAdapter<T>) new EventTagAdapter();
        }
        return null;
    }
}