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
package com.flipkart.batching.gson.adapters;

import android.support.annotation.Nullable;

import com.google.gson.Gson;
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
import java.util.Collection;
import java.util.Iterator;

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

    private static TypeAdapter<JSONObject> jsonObjectTypeAdapter;
    private static TypeAdapter<JSONArray> jsonArrayTypeAdapter;

    private static void write(Gson gson, JsonWriter out, @Nullable Object value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else if (value instanceof JSONObject) {
            getJSONObjectTypeAdapter(gson).write(out, (JSONObject) value);
        } else if (value instanceof JSONArray) {
            getJSONArrayTypeAdapter(gson).write(out, (JSONArray) value);
        } else if (value instanceof String) {
            TypeAdapters.STRING.write(out, (String) value);
        } else if (value instanceof Number) {
            TypeAdapters.NUMBER.write(out, (Number) value);
        } else if (value instanceof Boolean) {
            TypeAdapters.BOOLEAN.write(out, (Boolean) value);
        } else {
            gson.toJson(value, value.getClass(), out);
        }
    }

    public static TypeAdapter<JSONArray> getJSONArrayTypeAdapter(Gson gson) {
        if (jsonArrayTypeAdapter == null) {
            jsonArrayTypeAdapter = new JSONArrayTypeAdapter(gson);
        }
        return jsonArrayTypeAdapter;
    }

    public static TypeAdapter<JSONObject> getJSONObjectTypeAdapter(Gson gson) {
        if (jsonObjectTypeAdapter == null) {
            jsonObjectTypeAdapter = new JSONObjectTypeAdapter(gson);
        }
        return jsonObjectTypeAdapter;
    }

    public static final class JSONArrayTypeAdapter extends TypeAdapter<JSONArray> {

        private final Gson gson;

        public JSONArrayTypeAdapter(Gson gson) {
            this.gson = gson;
        }

        @Override
        public void write(JsonWriter out, JSONArray jsonArray) throws IOException {
            try {
                out.beginArray();
                for (int idx = 0; idx < jsonArray.length(); idx++) {
                    Object value = jsonArray.get(idx);
                    BatchingTypeAdapters.write(gson, out, value);
                }
                out.endArray();
            } catch (JSONException e) {
                throw new IOException(e);
            }
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
                        jsonArray.put(getJSONArrayTypeAdapter(gson).read(reader));
                        break;
                }
            }

            reader.endArray();
            return jsonArray;
        }
    }

    public static final class JSONObjectTypeAdapter extends TypeAdapter<JSONObject> {

        private final Gson gson;

        public JSONObjectTypeAdapter(Gson gson) {
            this.gson = gson;
        }

        @Override
        public void write(JsonWriter out, JSONObject jsonObject) throws IOException {
            out.beginObject();

            Iterator<String> iterator = jsonObject.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                try {
                    Object value = jsonObject.get(key);
                    out.name(key);
                    BatchingTypeAdapters.write(gson, out, value);
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
                            JSONArray jsonArray = getJSONArrayTypeAdapter(gson).read(reader);
                            jsonObject.put(name, jsonArray);
                            break;
                        case BEGIN_OBJECT:
                            jsonObject.put(name, read(reader));
                            break;
                    }
                }
            } catch (JSONException jse) {
                throw new IOException(jse);
            }

            reader.endObject();
            return jsonObject;
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