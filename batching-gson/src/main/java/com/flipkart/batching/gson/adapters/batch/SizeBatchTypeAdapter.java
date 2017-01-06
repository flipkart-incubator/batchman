package com.flipkart.batching.gson.adapters.batch;

import com.flipkart.batching.core.Data;
import com.flipkart.batching.core.DataCollection;
import com.flipkart.batching.core.batch.SizeBatch;
import com.flipkart.batching.gson.adapters.DataCollectionTypeAdapter;
import com.flipkart.batching.gson.adapters.KnownTypeAdapters;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public final class SizeBatchTypeAdapter<T extends Data> extends TypeAdapter<SizeBatch<T>> {
    private final TypeAdapter<DataCollection<T>> mTypeAdapter0;

    public SizeBatchTypeAdapter(TypeAdapter<T> typeAdapter0) {
        mTypeAdapter0 = new DataCollectionTypeAdapter<T>(typeAdapter0);
    }

    @Override
    public void write(JsonWriter writer, SizeBatch<T> object) throws IOException {
        writer.beginObject();
        if (object == null) {
            writer.endObject();
            return;
        }

        writer.name("maxBatchSize");
        writer.value(object.maxBatchSize);

        if (object.dataCollection != null) {
            writer.name("dataCollection");
            mTypeAdapter0.write(writer, object.dataCollection);
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

        SizeBatch<T> object = new SizeBatch<T>();
        while (reader.hasNext()) {
            String name = reader.nextName();
            com.google.gson.stream.JsonToken jsonToken = reader.peek();
            if (jsonToken == com.google.gson.stream.JsonToken.NULL) {
                reader.skipValue();
                continue;
            }
            switch (name) {
                case "maxBatchSize":
                    object.maxBatchSize = KnownTypeAdapters.INTEGER.read(reader);
                    break;
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