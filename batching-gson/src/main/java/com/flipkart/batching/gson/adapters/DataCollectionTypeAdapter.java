package com.flipkart.batching.gson.adapters;

import android.support.annotation.NonNull;

import com.flipkart.batching.core.Data;
import com.flipkart.batching.core.DataCollection;
import com.flipkart.batching.gson.RuntimeTypeAdapterFactory;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;

public class DataCollectionTypeAdapter<T extends Data> extends TypeAdapter<DataCollection<T>> {

    @NonNull
    private TypeAdapter<Collection<T>> collectionTypeAdapter;

    public DataCollectionTypeAdapter(@NonNull TypeAdapter<T> typeAdapter) {
        collectionTypeAdapter = new BatchingTypeAdapters.ListTypeAdapter<>(typeAdapter, new ObjectConstructor<Collection<T>>() {
            @Override
            public Collection<T> construct() {
                return new ArrayList<>();
            }
        });
    }

    @Override
    public void write(JsonWriter out, DataCollection<T> value) throws IOException {
        out.beginObject();
        if (value == null) {
            out.endObject();
            return;
        }

        if (value.dataCollection != null) {
            out.name("dataCollection");
            collectionTypeAdapter.write(out, value.dataCollection);
        }

        out.endObject();
    }

    @Override
    public DataCollection<T> read(JsonReader reader) throws IOException {
        if (reader.peek() == com.google.gson.stream.JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        if (reader.peek() != com.google.gson.stream.JsonToken.BEGIN_OBJECT) {
            reader.skipValue();
            return null;
        }
        reader.beginObject();

        Collection<T> collection = null;
        while (reader.hasNext()) {
            String name = reader.nextName();
            com.google.gson.stream.JsonToken jsonToken = reader.peek();
            if (jsonToken == com.google.gson.stream.JsonToken.NULL) {
                reader.skipValue();
                continue;
            }
            switch (name) {
                case "dataCollection":
                    collection = collectionTypeAdapter.read(reader);
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }

        reader.endObject();
        return null != collection ? new DataCollection<>(collection) : null;
    }
}