package com.flipkart.batching.gson.adapters;

import com.flipkart.batching.core.DataCollection;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

public class BatchingTypeAdapterFactory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<? super T> rawType = type.getRawType();
        if (rawType.isAssignableFrom(JSONObject.class)) {
            return (TypeAdapter<T>) BatchingTypeAdapters.getJSONObjectTypeAdapter(gson);
        } else if (rawType.isAssignableFrom(JSONArray.class)) {
            return (TypeAdapter<T>) BatchingTypeAdapters.getJSONArrayTypeAdapter(gson);
        }
        return null;
    }
}