package com.flipkart.batching.gson.adapters.batch;

import com.flipkart.batching.core.Data;
import com.flipkart.batching.core.DataCollection;
import com.flipkart.batching.core.batch.SizeBatch;
import com.flipkart.batching.gson.adapters.BatchingTypeAdapters;
import com.flipkart.batching.gson.adapters.DataCollectionTypeAdapter;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public final class SizeBatchTypeAdapter<T extends Data> extends TypeAdapter<SizeBatch<T>> {
    private final TypeAdapter<DataCollection<T>> typeAdapter;

    public SizeBatchTypeAdapter(TypeAdapter<T> typeAdapter) {
        this.typeAdapter = new DataCollectionTypeAdapter<T>(typeAdapter);
    }

    @Override
    public void write(JsonWriter writer, SizeBatch<T> object) throws IOException {
        writer.beginObject();
        if (object == null) {
            writer.endObject();
            return;
        }

        writer.name("maxBatchSize");
        writer.value(object.getMaxBatchSize());

        if (object.dataCollection != null) {
            writer.name("dataCollection");
            typeAdapter.write(writer, object.dataCollection);
        }

        writer.endObject();
    }

    @Override
    public SizeBatch<T> read(JsonReader reader) throws IOException {
        if (reader.peek() == com.google.gson.stream.JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        if (reader.peek() != com.google.gson.stream.JsonToken.BEGIN_OBJECT) {
            reader.skipValue();
            return null;
        }
        reader.beginObject();

        int maxBatchSize = 0;
        DataCollection<T> dataCollection = null;

        while (reader.hasNext()) {
            String name = reader.nextName();
            com.google.gson.stream.JsonToken jsonToken = reader.peek();
            if (jsonToken == com.google.gson.stream.JsonToken.NULL) {
                reader.skipValue();
                continue;
            }

            switch (name) {
                case "maxBatchSize":
                    maxBatchSize = BatchingTypeAdapters.INTEGER.read(reader);
                    break;
                case "dataCollection":
                    dataCollection = typeAdapter.read(reader);
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }

        reader.endObject();
        return dataCollection == null ? null : new SizeBatch<>(dataCollection.dataCollection, maxBatchSize);
    }
}