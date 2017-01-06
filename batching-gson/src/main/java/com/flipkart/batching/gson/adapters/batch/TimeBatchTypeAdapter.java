package com.flipkart.batching.gson.adapters.batch;

import com.flipkart.batching.core.Data;
import com.flipkart.batching.core.DataCollection;
import com.flipkart.batching.core.batch.TimeBatch;
import com.flipkart.batching.gson.adapters.DataCollectionTypeAdapter;
import com.flipkart.batching.gson.adapters.KnownTypeAdapters;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public final class TimeBatchTypeAdapter<D extends Data> extends TypeAdapter<TimeBatch<D>> {
    private final TypeAdapter<DataCollection<D>> typeAdapter;

    public TimeBatchTypeAdapter(TypeAdapter<D> typeAdapter0) {
        typeAdapter = new DataCollectionTypeAdapter<>(typeAdapter0);
    }

    @Override
    public void write(JsonWriter writer, TimeBatch<D> object) throws IOException {
        writer.beginObject();
        if (object == null) {
            writer.endObject();
            return;
        }

        writer.name("timeOut");
        writer.value(object.timeOut);

        if (object.dataCollection != null) {
            writer.name("dataCollection");
            typeAdapter.write(writer, object.dataCollection);
        }

        writer.endObject();
    }

    @Override
    public TimeBatch<D> read(JsonReader reader) throws IOException {
        if (reader.peek() == com.google.gson.stream.JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        if (reader.peek() != com.google.gson.stream.JsonToken.BEGIN_OBJECT) {
            reader.skipValue();
            return null;
        }
        reader.beginObject();

        TimeBatch<D> object = new TimeBatch<D>();
        while (reader.hasNext()) {
            String name = reader.nextName();
            com.google.gson.stream.JsonToken jsonToken = reader.peek();
            if (jsonToken == com.google.gson.stream.JsonToken.NULL) {
                reader.skipValue();
                continue;
            }
            switch (name) {
                case "timeOut":
                    object.timeOut = KnownTypeAdapters.LONG.read(reader);
                    break;
                case "dataCollection":
                    object.dataCollection = typeAdapter.read(reader);
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