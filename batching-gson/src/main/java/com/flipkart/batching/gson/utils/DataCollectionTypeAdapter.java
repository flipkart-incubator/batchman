package com.flipkart.batching.gson.utils;


import com.flipkart.batching.core.Data;
import com.flipkart.batching.core.DataCollection;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

public final class DataCollectionTypeAdapter<T extends Data> extends TypeAdapter<DataCollection<T>> {
    private final TypeAdapter<Collection<T>> mTypeAdapter0;

    public DataCollectionTypeAdapter(Gson gson, Type... type) {
        ParameterizedType parameterizedType = ParameterizedTypeUtil.newParameterizedTypeWithOwner(null, Collection.class, type[0]);
        mTypeAdapter0 = (TypeAdapter<Collection<T>>) gson.getAdapter(TypeToken.get(parameterizedType));
    }

    @Override
    public void write(JsonWriter writer, DataCollection<T> object) throws IOException {
        writer.beginObject();
        if (object == null) {
            writer.endObject();
            return;
        }
        if (object.dataCollection != null) {
            writer.name("dataCollection");
            mTypeAdapter0.write(writer, object.dataCollection);
        }
        writer.endObject();
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

        DataCollection<T> object = new DataCollection<>();
        while (reader.hasNext()) {
            String name = reader.nextName();
            com.google.gson.stream.JsonToken jsonToken = reader.peek();
            if (jsonToken == com.google.gson.stream.JsonToken.NULL) {
                reader.skipValue();
                continue;
            }
            switch (name) {
                case "dataCollection":
                    object.dataCollection = mTypeAdapter0.read(reader);
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }

        reader.endObject();
        return object;
    }
}