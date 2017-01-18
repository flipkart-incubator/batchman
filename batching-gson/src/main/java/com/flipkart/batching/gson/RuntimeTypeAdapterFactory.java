/*
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2017 Flipkart Internet Pvt. Ltd.
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

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RuntimeTypeAdapterFactory<T> implements TypeAdapterFactory {
    private final Class<?> baseType;
    private final Map<Class<?>, String> subtypeToLabel = new LinkedHashMap<>();
    private final Map<String, Class<?>> labelToSubtype = new LinkedHashMap<>();
    private final Map<String, TypeAdapter<? extends T>> labelToTypeAdapter = new LinkedHashMap<>();

    private RuntimeTypeAdapterFactory(Class<?> baseType) {
        if (baseType == null) {
            throw new NullPointerException();
        }
        this.baseType = baseType;
    }


    /**
     * Creates a new runtime type adapter for {@code baseType} using {@code "type"} as
     * the type field name.
     */
    public static <T> RuntimeTypeAdapterFactory<T> of(Class<T> baseType) {
        return new RuntimeTypeAdapterFactory<T>(baseType);
    }

    /**
     * Registers {@code type} identified by {@code label}. Labels are case
     * sensitive.
     *
     * @throws IllegalArgumentException if either {@code type} or {@code label}
     *                                  have already been registered on this type adapter.
     */
    public RuntimeTypeAdapterFactory<T> registerSubtype(Class<? extends T> type, String label) {
        if (type == null || label == null) {
            throw new NullPointerException();
        }
        if (subtypeToLabel.containsKey(type) || labelToSubtype.containsKey(label)) {
            throw new IllegalArgumentException("types and labels must be unique");
        }
        labelToSubtype.put(label, type);
        subtypeToLabel.put(type, label);
        return this;
    }

    /**
     * Registers {@code type} identified by {@code label}. Labels are case
     * sensitive.
     *
     * @throws IllegalArgumentException if either {@code type} or {@code label}
     *                                  have already been registered on this type adapter.
     */
    public RuntimeTypeAdapterFactory<T> registerSubtype(Class<? extends T> type, String label, TypeAdapter<? extends T> typeAdapter) {
        if (type == null || label == null || typeAdapter == null) {
            throw new NullPointerException();
        }
        if (subtypeToLabel.containsKey(type) || labelToSubtype.containsKey(label)) {
            throw new IllegalArgumentException("types and labels must be unique");
        }
        if (labelToTypeAdapter.containsKey(label)) {
            throw new IllegalStateException("TypeAdapter already registered for " + label);
        }
        labelToSubtype.put(label, type);
        subtypeToLabel.put(type, label);
        labelToTypeAdapter.put(label, typeAdapter);
        return this;
    }

    /**
     * Registers {@code type} identified by its {@link Class#getSimpleName simple
     * name}. Labels are case sensitive.
     *
     * @throws IllegalArgumentException if either {@code type} or its simple name
     *                                  have already been registered on this type adapter.
     */
    public RuntimeTypeAdapterFactory<T> registerSubtype(Class<? extends T> type) {
        return registerSubtype(type, type.getSimpleName());
    }

    /**
     * Registers {@code type} identified by its {@link Class#getSimpleName simple
     * name}. Labels are case sensitive.
     *
     * @throws IllegalArgumentException if either {@code type} or its simple name
     *                                  have already been registered on this type adapter.
     */
    public RuntimeTypeAdapterFactory<T> registerSubtype(Class<? extends T> type, TypeAdapter<? extends T> typeAdapter) {
        return registerSubtype(type, type.getSimpleName(), typeAdapter);
    }

    public <R> TypeAdapter<R> create(Gson gson, TypeToken<R> type) {
        if (type.getRawType() != baseType) {
            return null;
        }

        final Map<String, TypeAdapter<?>> labelToDelegate = new LinkedHashMap<>();
        final Map<Class<?>, TypeAdapter<?>> subtypeToDelegate = new LinkedHashMap<>();

        for (Map.Entry<String, Class<?>> entry : labelToSubtype.entrySet()) {
            TypeAdapter<?> delegate = labelToTypeAdapter.get(entry.getKey());
            if (delegate == null) {
                delegate = gson.getDelegateAdapter(this, TypeToken.get(entry.getValue()));
            }
            labelToDelegate.put(entry.getKey(), delegate);
            subtypeToDelegate.put(entry.getValue(), delegate);
        }

        return new TypeAdapter<R>() {
            @Override
            public R read(JsonReader reader) throws IOException {

                if (reader.peek() == com.google.gson.stream.JsonToken.NULL) {
                    reader.nextNull();
                    return null;
                }
                if (reader.peek() != com.google.gson.stream.JsonToken.BEGIN_OBJECT) {
                    reader.skipValue();
                    return null;
                }
                reader.beginObject();

                String label = null;
                R result = null;
                while (reader.hasNext()) {
                    String name = reader.nextName();
                    com.google.gson.stream.JsonToken jsonToken = reader.peek();
                    if (jsonToken == com.google.gson.stream.JsonToken.NULL) {
                        reader.skipValue();
                        continue;
                    }
                    switch (name) {
                        case "type":
                            label = TypeAdapters.STRING.read(reader);
                            break;
                        case "value":
                            @SuppressWarnings("unchecked") // registration requires that subtype extends T
                                    TypeAdapter<R> delegate = label == null ? null : (TypeAdapter<R>) labelToDelegate.get(label);
                            if (delegate == null) {
                                throw new JsonParseException("cannot deserialize " + baseType + " subtype named "
                                        + label + "; did you forget to register a subtype?");
                            }
                            result = delegate.read(reader);
                            break;
                        default:
                            reader.skipValue();
                            break;
                    }
                }

                reader.endObject();
                return result;
            }

            @Override
            public void write(JsonWriter out, R value) throws IOException {
                Class<?> srcType = value.getClass();
                @SuppressWarnings("unchecked") // registration requires that subtype extends T
                        TypeAdapter<R> delegate = (TypeAdapter<R>) subtypeToDelegate.get(srcType);
                if (delegate == null) {
                    throw new JsonParseException("cannot serialize " + srcType.getName()
                            + "; did you forget to register a subtype?");
                }
                String label = subtypeToLabel.get(srcType);
                out.beginObject();

                out.name("type");
                out.value(label);

                out.name("value");
                delegate.write(out, value);

                out.endObject();
            }
        }.nullSafe();
    }
}