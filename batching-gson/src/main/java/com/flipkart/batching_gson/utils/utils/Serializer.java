package com.flipkart.batching_gson.utils.utils;

import com.flipkart.batchingcore.Data;
import com.flipkart.batchingcore.DataCollection;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collection;

public class Serializer implements JsonSerializer<DataCollection> {
    @Override
    public JsonElement serialize(DataCollection src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = new JsonObject();
        JsonElement dataCollectionArray = null;
        if (null != src.dataCollection) {
            Type type = new TypeToken<Collection<Data>>() {
            }.getType();
            dataCollectionArray = context.serialize(src.dataCollection, type);
        }
        result.add("dataCollection", dataCollectionArray);
        return result;
    }
}