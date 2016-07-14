/*
 * Copyright 2016 Flipkart Internet Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 * DataCollection class
 */

public class DataCollection<T extends Data> {
    @SerializedName("dataCollection")
    Collection<T> dataCollection;

    DataCollection(Collection<T> dataCollection) {
        this.dataCollection = dataCollection;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DataCollection) {
            return dataCollection.equals(((DataCollection) o).dataCollection);
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return dataCollection == null ? 0 : dataCollection.hashCode();
    }

    public static class Serializer implements JsonSerializer<DataCollection> {
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

    public static class DeSerializer implements JsonDeserializer<DataCollection> {
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
}
