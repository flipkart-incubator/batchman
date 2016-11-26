package com.flipkart.batching.gson.utils;

import com.flipkart.batching.core.Data;
import com.flipkart.batching.core.DataCollection;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collection;

public class Deserializer implements JsonDeserializer<DataCollection> {
    @Override
    public DataCollection deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (null != json && json.isJsonObject()) {
            JsonElement dataCollectionJson = json.getAsJsonObject().get("dataCollection");
            if (null != dataCollectionJson) {
                Type type = new TypeToken<Collection<Data>>() {
                }.getType();
                Collection dataCollection = context.deserialize(dataCollectionJson, type);
                return new DataCollection(dataCollection);
            }
        }
        return null;
    }
}