package com.flipkart.batching.gson.adapters.batch;

import com.flipkart.batching.core.Data;
import com.flipkart.batching.core.DataCollection;
import com.flipkart.batching.core.batch.TimeBatch;
import com.flipkart.batching.gson.adapters.BatchingTypeAdapters;
import com.flipkart.batching.gson.adapters.DataCollectionTypeAdapter;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public final class TimeBatchTypeAdapter<T extends Data> extends TypeAdapter<TimeBatch<T>> {
    private final TypeAdapter<DataCollection<T>> typeAdapter;

    public TimeBatchTypeAdapter(TypeAdapter<T> typeAdapter) {
        this.typeAdapter = new DataCollectionTypeAdapter<T>(typeAdapter);
    }

    @Override
    public void write(JsonWriter writer, TimeBatch<T> object) throws IOException {
        writer.beginObject();
        if (object == null) {
            writer.endObject();
            return;
        }

        writer.name("timeOut");
        writer.value(object.getTimeOut());

        if (object.dataCollection != null) {
            writer.name("dataCollection");
            typeAdapter.write(writer, object.dataCollection);
        }

        writer.endObject();
    }

    @Override
    public TimeBatch<T> read(JsonReader reader) throws IOException {
        if (reader.peek() == com.google.gson.stream.JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        if (reader.peek() != com.google.gson.stream.JsonToken.BEGIN_OBJECT) {
            reader.skipValue();
            return null;
        }
        reader.beginObject();

        long timeOut = 0L;
        DataCollection<T> dataCollection = null;

        while (reader.hasNext()) {
            String name = reader.nextName();
            com.google.gson.stream.JsonToken jsonToken = reader.peek();
            if (jsonToken == com.google.gson.stream.JsonToken.NULL) {
                reader.skipValue();
                continue;
            }
            switch (name) {
                case "timeOut":
                    timeOut = BatchingTypeAdapters.LONG.read(reader);
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
        return dataCollection == null ? null : new TimeBatch<>(dataCollection.dataCollection, timeOut);
    }
}