package com.flipkart.batching.persistence;

import com.flipkart.batching.Batch;
import com.flipkart.batching.Data;
import com.flipkart.batching.DataCollection;
import com.flipkart.batching.exception.DeserializeException;
import com.flipkart.batching.exception.SerializeException;
import com.flipkart.batching.toolbox.RuntimeTypeAdapterFactory;
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
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of {@link SerializationStrategy}.
 *
 * @see SerializationStrategy
 */

public class GsonSerializationStrategy<E extends Data, T extends Batch> implements SerializationStrategy<E, T> {

    private static final String IS_JSON_OBJECT = "_com.flipkart.batching.isJsonObject";

    Set<Class<E>> dataTypes = new HashSet<>();
    Set<Class<T>> batchInfoTypes = new HashSet<>();
    private Gson gson;

    @Override
    public void registerDataType(Class<E> subClass) {
        dataTypes.add(subClass);
    }

    @Override
    public void registerBatch(Class<T> subClass) {
        batchInfoTypes.add(subClass);
    }

    @Override
    public void build() {
        RuntimeTypeAdapterFactory<E> dataAdapter = (RuntimeTypeAdapterFactory<E>) RuntimeTypeAdapterFactory.of(Data.class);
        for (Class<E> dataType : dataTypes) {
            dataAdapter.registerSubtype(dataType);
        }

        RuntimeTypeAdapterFactory<T> batchInfoAdapter = (RuntimeTypeAdapterFactory<T>) RuntimeTypeAdapterFactory.of(Batch.class);
        for (Class<T> batchInfoType : batchInfoTypes) {
            batchInfoAdapter.registerSubtype(batchInfoType);
        }

        RuntimeTypeAdapterFactory<Collection> collectionAdapter = RuntimeTypeAdapterFactory.of(Collection.class);
        collectionAdapter.registerSubtype(ArrayList.class);

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapterFactory(dataAdapter);
        gsonBuilder.registerTypeAdapterFactory(batchInfoAdapter);
        gsonBuilder.registerTypeAdapter(DataCollection.class, new DataCollection.Serializer());
        gsonBuilder.registerTypeAdapter(DataCollection.class, new DataCollection.DeSerializer());
        gsonBuilder.registerTypeAdapter(JSONObject.class, new JSONDeSerializer());
        gsonBuilder.registerTypeAdapter(JSONObject.class, new JSONSerializer());
        //gsonBuilder.registerTypeAdapterFactory(collectionAdapter);
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
            return gson.toJson(data, Data.class).getBytes();
        } catch (JsonParseException e) {
            throw new SerializeException(e);
        }
    }

    @Override
    public byte[] serializeCollection(Collection<E> data) throws SerializeException {
        checkIfBuildCalled();
        Type type = new TypeToken<Collection<Data>>() {
        }.getType();
        try {
            return gson.toJson(data, type).getBytes();
        } catch (JsonParseException e) {
            throw new SerializeException(e);
        }
    }

    @Override
    public byte[] serializeBatch(T batch) throws SerializeException {
        checkIfBuildCalled();
        try {
            return gson.toJson(batch, Batch.class).getBytes();
        } catch (JsonParseException e) {
            throw new SerializeException(e);
        }
    }

    @Override
    public E deserializeData(byte[] data) throws DeserializeException {
        checkIfBuildCalled();
        try {
            return (E) gson.fromJson(new String(data), Data.class);
        } catch (JsonParseException e) {
            throw new DeserializeException(e);
        }
    }

    @Override
    public Collection<E> deserializeCollection(byte[] data) throws DeserializeException {
        checkIfBuildCalled();

        Type type = new TypeToken<Collection<Data>>() {
        }.getType();
        try {
            return gson.fromJson(new String(data), type);
        } catch (JsonParseException e) {
            throw new DeserializeException(e);
        }
    }

    @Override
    public T deserializeBatch(byte[] data) throws DeserializeException {
        checkIfBuildCalled();
        try {
            return (T) gson.fromJson(new String(data), Batch.class);
        } catch (JsonParseException e) {
            throw new DeserializeException(e);
        }
    }


    public static class JSONSerializer implements JsonSerializer<JSONObject> {
        @Override
        public JsonElement serialize(JSONObject src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = null;
            if (null != src) {
                try {
                    result = new JsonObject();
                    Iterator<String> iterator = src.keys();
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        Object value = src.get(key);
                        if ((null != value && value instanceof JSONObject)) {
                            JsonElement jsonObject = context.serialize(value, JSONObject.class);
                            if (null != jsonObject && jsonObject.isJsonObject()) {
                                jsonObject.getAsJsonObject().addProperty(IS_JSON_OBJECT, true);
                            }
                            result.add(key, jsonObject);
                        } else {
                            result.add(key, context.serialize(value));
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return result;

        }
    }

    public static class JSONDeSerializer implements JsonDeserializer<JSONObject> {

        private static Object getObjectFromJsonElement(JsonElement value, JsonDeserializationContext context) {
            Object result = null;
            if (value.isJsonObject()) {
                if (value.getAsJsonObject().has("_com.flipkart.batching.isJsonObject")) {
                    JSONObject jsonObject = context.deserialize(value, JSONObject.class);
                    if (null != jsonObject) {
                        jsonObject.remove("_com.flipkart.batching.isJsonObject");
                    }
                    return jsonObject;
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

        @Override
        public JSONObject deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
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
    }
}
