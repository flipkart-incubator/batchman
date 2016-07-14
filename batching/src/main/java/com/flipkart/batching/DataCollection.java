/*
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2016 Flipkart Internet Pvt. Ltd.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
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
