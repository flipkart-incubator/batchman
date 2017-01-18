package com.flipkart.batching.gson.adapters.batch;

import com.flipkart.batching.core.Data;
import com.flipkart.batching.core.DataCollection;
import com.flipkart.batching.core.batch.SizeTimeBatch;
import com.flipkart.batching.gson.RuntimeTypeAdapterFactory;
import com.flipkart.batching.gson.adapters.DataCollectionTypeAdapter;
import com.flipkart.batching.gson.adapters.BatchingTypeAdapters;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public final class SizeTimeBatchTypeAdapter<T extends Data> extends TypeAdapter<SizeTimeBatch<T>> {
    private final TypeAdapter<DataCollection<T>> typeAdapter;

    public SizeTimeBatchTypeAdapter(TypeAdapter<T> typeAdapter) {
        this.typeAdapter = new DataCollectionTypeAdapter<T>(typeAdapter);
    }

    @Override
    public void write(JsonWriter writer, SizeTimeBatch<T> object) throws IOException {
        writer.beginObject();
        if (object == null) {
            writer.endObject();
            return;
        }

        writer.name("maxBatchSize");
        writer.value(object.maxBatchSize);

        if (object.dataCollection != null) {
            writer.name("dataCollection");
            typeAdapter.write(writer, object.dataCollection);
        }

        writer.name("timeOut");
        writer.value(object.timeOut);

        writer.endObject();
    }

    @Override
    public SizeTimeBatch<T> read(JsonReader reader) throws IOException {
        if (reader.peek() == com.google.gson.stream.JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        if (reader.peek() != com.google.gson.stream.JsonToken.BEGIN_OBJECT) {
            reader.skipValue();
            return null;
        }
        reader.beginObject();

        SizeTimeBatch<T> object = new SizeTimeBatch<T>();
        while (reader.hasNext()) {
            String name = reader.nextName();
            com.google.gson.stream.JsonToken jsonToken = reader.peek();
            if (jsonToken == com.google.gson.stream.JsonToken.NULL) {
                reader.skipValue();
                continue;
            }
            switch (name) {
                case "maxBatchSize":
                    object.maxBatchSize = BatchingTypeAdapters.INTEGER.read(reader);
                    break;
                case "dataCollection":
                    object.dataCollection = typeAdapter.read(reader);
                    break;
                case "timeOut":
                    object.timeOut = BatchingTypeAdapters.LONG.read(reader);
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