package com.flipkart.batching.gson.adapters;

import com.flipkart.batching.core.BatchImpl;
import com.flipkart.batching.core.Data;
import com.flipkart.batching.core.DataCollection;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public final class BatchImplTypeAdapter<T extends Data> extends TypeAdapter<BatchImpl<T>> {
    private final TypeAdapter<DataCollection<T>> typeAdapter;

    public BatchImplTypeAdapter(TypeAdapter<T> typeAdapter) {
        this.typeAdapter = new DataCollectionTypeAdapter<>(typeAdapter);
    }

    @Override
    public void write(JsonWriter writer, BatchImpl<T> object) throws IOException {
        writer.beginObject();
        if (object == null) {
            writer.endObject();
            return;
        }

        if (object.dataCollection != null) {
            writer.name("dataCollection");
            typeAdapter.write(writer, object.dataCollection);
        }

        writer.endObject();
    }

    @Override
    public BatchImpl<T> read(JsonReader reader) throws IOException {
        if (reader.peek() == com.google.gson.stream.JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        if (reader.peek() != com.google.gson.stream.JsonToken.BEGIN_OBJECT) {
            reader.skipValue();
            return null;
        }
        reader.beginObject();

        DataCollection<T> dataCollection = null;
        while (reader.hasNext()) {
            String name = reader.nextName();
            com.google.gson.stream.JsonToken jsonToken = reader.peek();
            if (jsonToken == com.google.gson.stream.JsonToken.NULL) {
                reader.skipValue();
                continue;
            }
            switch (name) {
                case "dataCollection":
                    dataCollection = typeAdapter.read(reader);
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }

        reader.endObject();
        return dataCollection == null ? null : new BatchImpl<T>(dataCollection.dataCollection);
    }
}