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

package com.flipkart.batching.gson;

import com.flipkart.batching.core.Batch;
import com.flipkart.batching.core.Data;
import com.flipkart.batching.core.SerializationStrategy;
import com.flipkart.batching.core.exception.DeserializeException;
import com.flipkart.batching.core.exception.SerializeException;
import com.flipkart.batching.gson.adapters.KnownTypeAdapters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.LinkedTreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Implementation of {@link SerializationStrategy}.
 *
 * @see SerializationStrategy
 */
public class GsonSerializationStrategy<E extends Data, T extends Batch> implements SerializationStrategy<E, T> {
    private static final String IS_JSON_OBJECT = "_com.flipkart.batching.isJsonObject";
    private static final String JSON_ARRAY_OBJECT = "_com.flipkart.batching.jsonArray";
    private Gson gson;
    private TypeAdapter<E> dataTypeAdapter;
    private TypeAdapter<T> batchTypeAdapter;
    private TypeAdapter<Collection<E>> collectionTypeAdapter;

    public static JsonElement serializeJSONArray(JSONArray src, JsonSerializationContext context) {
        JsonObject result = null;
        try {
            if (null != src) {
                result = new JsonObject();
                JsonArray jsonArray = new JsonArray();
                for (int idx = 0; idx < src.length(); idx++) {
                    Object value = src.get(idx);
                    JsonElement element = forJSONGenericObject(value, context);
                    jsonArray.add(element);
                }
                result.add(JSON_ARRAY_OBJECT, jsonArray);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static JSONArray deserializeJSONArray(JsonElement json, JsonDeserializationContext context) throws JsonParseException {
        JSONArray result = null;
        if (null != json) {
            result = new JSONArray();
            if (json.isJsonObject() && json.getAsJsonObject().has(JSON_ARRAY_OBJECT)) {
                JsonArray jsonArray = json.getAsJsonObject().getAsJsonArray(JSON_ARRAY_OBJECT);
                for (JsonElement element : jsonArray) {
                    if (null != element) {
                        result.put(getObjectFromJsonElement(element, context));
                    } else {
                        result.put(null);
                    }
                }
            }
        }
        return result;
    }

    public static JsonElement serializeJSONObject(JSONObject src, JsonSerializationContext context) {
        JsonObject result = null;
        if (null != src) {
            try {
                result = new JsonObject();
                Iterator<String> iterator = src.keys();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    Object value = src.get(key);
                    JsonElement element = forJSONGenericObject(value, context);
                    result.add(key, element);
                    result.addProperty(IS_JSON_OBJECT, true);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return result;

    }

    public static JSONObject deserializeJSONObject(JsonElement json, JsonDeserializationContext context) throws JsonParseException {
        JSONObject result = null;
        if (null != json && json.isJsonObject()) {
            try {
                result = new JSONObject();
                JsonObject jsonObject = json.getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                    JsonElement value = entry.getValue();
                    if (null != value) {
                        result.put(entry.getKey(), getObjectFromJsonElement(entry.getValue(), context));
                    } else {
                        result.put(entry.getKey(), null);
                    }
                }
                result.remove(IS_JSON_OBJECT);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return result;
    }

    private static Object getObjectFromJsonElement(JsonElement value, JsonDeserializationContext context) {
        Object result = null;
        if (value.isJsonObject()) {
            if (value.getAsJsonObject().has(IS_JSON_OBJECT)) {
                JSONObject jsonObject = deserializeJSONObject(value, context);
                if (null != jsonObject) {
                    jsonObject.remove(IS_JSON_OBJECT);
                }
                return jsonObject;
            } else if (value.getAsJsonObject().has(JSON_ARRAY_OBJECT)) {
                return deserializeJSONArray(value, context);
            } else {
                result = getMapFromJson(value.getAsJsonObject(), context);
            }

        } else if (value.isJsonPrimitive()) {
            JsonPrimitive primitiveValue = value.getAsJsonPrimitive();
            if (primitiveValue.isBoolean()) {
                result = primitiveValue.getAsBoolean();
            } else if (primitiveValue.isNumber()) {
                result = primitiveValue.getAsNumber();
            } else {
                result = primitiveValue.getAsString();
            }
        } else if (value.isJsonNull()) {
            result = null;
        } else if (value.isJsonArray()) {
            JsonArray jsonArray = value.getAsJsonArray();
            ArrayList<Object> list = new ArrayList<>(jsonArray.size());
            for (JsonElement element : jsonArray) {
                list.add(getObjectFromJsonElement(element, context));
            }
            result = list;
        }
        return result;
    }

    public static Map<String, Object> getMapFromJson(JsonObject data, JsonDeserializationContext context) {
        Map<String, Object> result = null;
        if (null != data) {
            result = new LinkedTreeMap<>();
            for (Map.Entry<String, JsonElement> entry : data.entrySet()) {
                result.put(entry.getKey(), getObjectFromJsonElement(entry.getValue(), context));
            }
        }
        return result;
    }

    public static JsonElement forJSONGenericObject(Object value, JsonSerializationContext context) {
        JsonElement element;
        if (null != value) {
            if (value instanceof JSONObject) {
                element = serializeJSONObject((JSONObject) value, context);
            } else if (value instanceof JSONArray) {
                element = serializeJSONArray((JSONArray) value, context);
            } else if (value instanceof String) {
                element = new JsonPrimitive((String) value);
            } else if (value instanceof Number) {
                element = new JsonPrimitive((Number) value);
            } else if (value instanceof Boolean) {
                element = new JsonPrimitive((Boolean) value);
            } else {
                element = context.serialize(value);
            }
        } else {
            element = context.serialize(value);
        }
        return element;
    }

    private TypeAdapter<Collection<E>> getCollectionTypeAdapter() {
        if (collectionTypeAdapter == null) {
            collectionTypeAdapter = new KnownTypeAdapters.ListTypeAdapter<E, Collection<E>>(getDataTypeAdapter(), new KnownTypeAdapters.CollectionInstantiater<E>());
        }
        return collectionTypeAdapter;
    }

    private TypeAdapter<E> getDataTypeAdapter() {
        if (dataTypeAdapter == null) {
            dataTypeAdapter = (TypeAdapter<E>) gson.getAdapter(Data.class);
        }
        return dataTypeAdapter;
    }

    private TypeAdapter<T> getBatchTypeAdapter() {
        if (batchTypeAdapter == null) {
            batchTypeAdapter = (TypeAdapter<T>) gson.getAdapter(Batch.class);
        }
        return batchTypeAdapter;
    }

    @Override
    public void build() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapterFactory(new BatchingTypeAdapterFactory());

        gsonBuilder.registerTypeAdapter(JSONObject.class, new JSONObjectDeSerializer());
        gsonBuilder.registerTypeAdapter(JSONObject.class, new JSONObjectSerializer());
        gsonBuilder.registerTypeAdapter(JSONArray.class, new JSONArrayDeSerializer());
        gsonBuilder.registerTypeAdapter(JSONArray.class, new JSONArraySerializer());
        gson = gsonBuilder.create();
    }

    private void checkIfBuildCalled() {
        if (gson == null) {
            throw new IllegalStateException("The build() method was not called on " + getClass());
        }
    }

    @Override
    public byte[] serializeData(E data) throws SerializeException {
        checkIfBuildCalled();
        try {
            return getDataTypeAdapter().toJson(data).getBytes();
        } catch (JsonParseException e) {
            throw new SerializeException(e);
        }
    }

    @Override
    public byte[] serializeCollection(Collection<E> data) throws SerializeException {
        checkIfBuildCalled();
        try {
            return getCollectionTypeAdapter().toJson(data).getBytes();
        } catch (JsonParseException e) {
            throw new SerializeException(e);
        }
    }

    @Override
    public byte[] serializeBatch(T batch) throws SerializeException {
        checkIfBuildCalled();
        try {
            return getBatchTypeAdapter().toJson(batch).getBytes();
        } catch (JsonParseException e) {
            throw new SerializeException(e);
        }
    }

    @Override
    public E deserializeData(byte[] data) throws DeserializeException {
        checkIfBuildCalled();
        try {
            return getDataTypeAdapter().fromJson(new String(data));
        } catch (IOException e) {
            throw new DeserializeException(e);
        }
    }

    @Override
    public Collection<E> deserializeCollection(byte[] data) throws DeserializeException {
        checkIfBuildCalled();
        try {
            return getCollectionTypeAdapter().fromJson(new String(data));
        } catch (IOException e) {
            throw new DeserializeException(e);
        }
    }

    @Override
    public T deserializeBatch(byte[] data) throws DeserializeException {
        checkIfBuildCalled();
        try {
            return getBatchTypeAdapter().fromJson(new String(data));
        } catch (IOException e) {
            throw new DeserializeException(e);
        }
    }

    public static class JSONObjectSerializer implements JsonSerializer<JSONObject> {
        @Override
        public JsonElement serialize(JSONObject src, Type typeOfSrc, JsonSerializationContext context) {
            return serializeJSONObject(src, context);
        }
    }


    public static class JSONObjectDeSerializer implements JsonDeserializer<JSONObject> {
        @Override
        public JSONObject deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return deserializeJSONObject(json, context);
        }
    }

    public static class JSONArraySerializer implements JsonSerializer<JSONArray> {
        @Override
        public JsonElement serialize(JSONArray src, Type typeOfSrc, JsonSerializationContext context) {
            return serializeJSONArray(src, context);
        }
    }

    public static class JSONArrayDeSerializer implements JsonDeserializer<JSONArray> {
        @Override
        public JSONArray deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return deserializeJSONArray(json, context);
        }
    }
}