/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flipkart.batching.gson.adapters;

import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.LazilyParsedNumber;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static com.google.gson.stream.JsonToken.BEGIN_ARRAY;
import static com.google.gson.stream.JsonToken.BEGIN_OBJECT;

public class BatchingTypeAdapters {

    public static final TypeAdapter<Integer> INTEGER = new TypeAdapter<Integer>() {
        @Override
        public Integer read(JsonReader in) throws IOException {
            try {
                return in.nextInt();
            } catch (NumberFormatException e) {
                throw new JsonSyntaxException(e);
            }
        }

        @Override
        public void write(JsonWriter out, Integer value) throws IOException {
            out.value(value);
        }
    }.nullSafe();

    public static final TypeAdapter<Long> LONG = new TypeAdapter<Long>() {
        @Override
        public Long read(JsonReader in) throws IOException {
            try {
                return in.nextLong();
            } catch (NumberFormatException e) {
                throw new JsonSyntaxException(e);
            }
        }

        @Override
        public void write(JsonWriter out, Long value) throws IOException {
            out.value(value);
        }
    }.nullSafe();

    public static final TypeAdapter<JSONArray> JSON_ARRAY_TYPE_ADAPTER = new TypeAdapter<JSONArray>() {

        @Override
        public void write(JsonWriter out, JSONArray jsonArray) throws IOException {
            out.beginArray();

            for (int idx = 0; idx < jsonArray.length(); idx++) {
                Object value = null;
                try {
                    value = jsonArray.get(idx);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (value instanceof JSONObject) {
                    JSON_OBJECT_TYPE_ADAPTER.write(out, (JSONObject) value);
                } else if (value instanceof JSONArray) {
                    write(out, (JSONArray) value);
                } else if (value instanceof String) {
                    TypeAdapters.STRING.write(out, (String) value);
                } else if (value instanceof Number) {
                    TypeAdapters.NUMBER.write(out, (Number) value);
                } else if (value instanceof Boolean) {
                    TypeAdapters.BOOLEAN.write(out, (Boolean) value);
                } else {
                    out.nullValue();
                }
            }

            out.endArray();
        }

        @Override
        public JSONArray read(JsonReader reader) throws IOException {
            if (reader.peek() != BEGIN_ARRAY) {
                reader.skipValue();
                return null;
            }

            reader.beginArray();
            JSONArray jsonArray = new JSONArray();

            while (reader.hasNext()) {
                com.google.gson.stream.JsonToken jsonToken = reader.peek();
                if (jsonToken == com.google.gson.stream.JsonToken.NULL) {
                    reader.skipValue();
                    continue;
                }

                switch (jsonToken) {
                    case NUMBER:
                        jsonArray.put(new LazilyParsedNumber(reader.nextString()));
                        break;
                    case BOOLEAN:
                        jsonArray.put(TypeAdapters.BOOLEAN.read(reader));
                        break;
                    case STRING:
                        jsonArray.put(TypeAdapters.STRING.read(reader));
                        break;
                    case BEGIN_ARRAY:
                        jsonArray.put(read(reader));
                        break;
                    case BEGIN_OBJECT:
                        jsonArray.put(JSON_OBJECT_TYPE_ADAPTER.read(reader));
                        break;
                }
            }

            reader.endArray();
            return jsonArray;
        }
    }.nullSafe();

    public static final TypeAdapter<JSONObject> JSON_OBJECT_TYPE_ADAPTER = new TypeAdapter<JSONObject>() {

        @Override
        public void write(JsonWriter out, JSONObject jsonObject) throws IOException {
            out.beginObject();

            Iterator<String> iterator = jsonObject.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                try {
                    Object value = jsonObject.get(key);
                    out.name(key);
                    if (value instanceof JSONObject) {
                        write(out, (JSONObject) value);
                    } else if (value instanceof JSONArray) {
                        JSON_ARRAY_TYPE_ADAPTER.write(out, (JSONArray) value);
                    } else if (value instanceof String) {
                        TypeAdapters.STRING.write(out, (String) value);
                    } else if (value instanceof Number) {
                        TypeAdapters.NUMBER.write(out, (Number) value);
                    } else if (value instanceof Boolean) {
                        TypeAdapters.BOOLEAN.write(out, (Boolean) value);
                    } else {
                        out.nullValue();
                    }
                } catch (JSONException e) {
                    throw new IOException(e);
                }
            }

            out.endObject();
        }

        @Override
        public JSONObject read(JsonReader reader) throws IOException {
            if (reader.peek() != BEGIN_OBJECT) {
                reader.skipValue();
                return null;
            }

            reader.beginObject();
            JSONObject jsonObject = new JSONObject();

            try {
                while (reader.hasNext()) {
                    String name = reader.nextName();
                    JsonToken jsonToken = reader.peek();
                    if (jsonToken == JsonToken.NULL) {
                        reader.skipValue();
                        continue;
                    }
                    switch (jsonToken) {
                        case NUMBER:
                            jsonObject.put(name, new LazilyParsedNumber(reader.nextString()));
                            break;
                        case BOOLEAN:
                            jsonObject.put(name, TypeAdapters.BOOLEAN.read(reader));
                            break;
                        case STRING:
                            jsonObject.put(name, TypeAdapters.STRING.read(reader));
                            break;
                        case BEGIN_ARRAY:
                            JSONArray jsonArray = JSON_ARRAY_TYPE_ADAPTER.read(reader);
                            jsonObject.put(name, jsonArray);
                            break;
                        case BEGIN_OBJECT:
                            JSONObject object = read(reader);
                            jsonObject.put(name, object);
                            break;
                    }
                }
            } catch (JSONException jse) {
                throw new IOException(jse);
            }
            reader.endObject();
            return jsonObject;
        }
    }.nullSafe();

    /**
     * Default Instantiater for List, by default it will create the Map of {@link ArrayList} type
     */
    public static class ListInstantiater<V> implements ObjectConstructor<List<V>> {
        @Override
        public List<V> construct() {
            return new ArrayList<V>();
        }
    }

    /**
     * Instantiater for {@link Collection}
     */
    public static class CollectionInstantiater<V> implements ObjectConstructor<Collection<V>> {
        @Override
        public Collection<V> construct() {
            return new ArrayList<V>();
        }
    }

    /**
     * Instantiater for {@link ArrayList}
     */
    public static class ArrayListInstantiater<V> implements ObjectConstructor<ArrayList<V>> {
        @Override
        public ArrayList<V> construct() {
            return new ArrayList<V>();
        }
    }

    /**
     * Type Adapter for {@link Collection}
     */
    public static class ListTypeAdapter<V, T extends Collection<V>> extends TypeAdapter<T> {

        private TypeAdapter<V> valueTypeAdapter;
        private ObjectConstructor<T> objectConstructor;

        public ListTypeAdapter(TypeAdapter<V> valueTypeAdapter, ObjectConstructor<T> objectConstructor) {
            this.valueTypeAdapter = valueTypeAdapter;
            this.objectConstructor = objectConstructor;
        }

        @Override
        public void write(JsonWriter writer, T value) throws IOException {
            writer.beginArray();
            if (null != value) {
                for (V item : value) {
                    valueTypeAdapter.write(writer, item);
                }
            }
            writer.endArray();
        }

        @Override
        public T read(JsonReader reader) throws IOException {
            if (reader.peek() == JsonToken.NULL) {
                reader.nextNull();
                return null;
            }

            if (reader.peek() != BEGIN_ARRAY) {
                reader.skipValue();
                return null;
            }

            T collection = objectConstructor.construct();
            reader.beginArray();
            while (reader.hasNext()) {
                collection.add(valueTypeAdapter.read(reader));
            }
            reader.endArray();
            return collection;
        }
    }
}