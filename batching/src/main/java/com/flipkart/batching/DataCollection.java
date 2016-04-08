package com.flipkart.batching;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collection;

/**
 * Created by kushal.sharma on 01/03/16.
 * Data Collection
 */

public class DataCollection<T extends Data> {

    @SerializedName("dataCollection")
    Collection<T> dataCollection;

    DataCollection(Collection<T> dataCollection) {
        this.dataCollection = dataCollection;
    }

    private static final Type TYPE_COLLECTION_DATA = new TypeToken<Collection<Data>>() {
    }.getType();

    @Override
    public boolean equals(Object o) {
        if (o instanceof DataCollection) {
            return dataCollection.equals(((DataCollection) o).dataCollection);
        }
        return super.equals(o);
    }

    public static class Serializer implements JsonSerializer<DataCollection> {
        @Override
        public JsonElement serialize(DataCollection src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            JsonElement dataCollectionArray = null;
            if (null != src.dataCollection) {
                dataCollectionArray = context.serialize(src.dataCollection, TYPE_COLLECTION_DATA);
            }
            result.add("dataCollection", dataCollectionArray);
            return result;
        }
    }

    public static class DeSerializer implements JsonDeserializer<DataCollection> {

        @Override
        public DataCollection deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (null != json && json.isJsonObject()) {
                JsonElement dataCollectionJson = json.getAsJsonObject().get("dataCollection");
                if (null != dataCollectionJson) {
                    Collection dataCollection = context.deserialize(dataCollectionJson, TYPE_COLLECTION_DATA);
                    return new DataCollection(dataCollection);
                }
            }
            return null;
        }
    }
}
