package com.flipkart.batching.gson.adapters.batch;

import com.flipkart.batching.core.DataCollection;
import com.flipkart.batching.core.batch.TagBatch;
import com.flipkart.batching.core.data.TagData;
import com.flipkart.batching.gson.BatchingTypeAdapterFactory;
import com.flipkart.batching.gson.adapters.DataCollectionTypeAdapter;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public final class TagBatchTypeAdapter<T extends TagData> extends TypeAdapter<TagBatch<T>> {
    private final TypeAdapter<DataCollection<T>> typeAdapter;

    private final BatchingTypeAdapterFactory batchingTypeAdapterFactory;

    public TagBatchTypeAdapter(BatchingTypeAdapterFactory batchingTypeAdapterFactory, TypeAdapter<T> typeAdapter0) {
        this.typeAdapter = new DataCollectionTypeAdapter<>(typeAdapter0);
        this.batchingTypeAdapterFactory = batchingTypeAdapterFactory;
    }

    @Override
    public void write(JsonWriter writer, TagBatch<T> object) throws IOException {
        writer.beginObject();
        if (object == null) {
            writer.endObject();
            return;
        }

        if (object.tag != null) {
            writer.name("tag");
            batchingTypeAdapterFactory.getTagTypeAdapter().write(writer, object.tag);
        }

        if (object.dataCollection != null) {
            writer.name("dataCollection");
            typeAdapter.write(writer, object.dataCollection);
        }

        writer.endObject();
    }

    @Override
    public TagBatch<T> read(JsonReader reader) throws IOException {
        if (reader.peek() == com.google.gson.stream.JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        if (reader.peek() != com.google.gson.stream.JsonToken.BEGIN_OBJECT) {
            reader.skipValue();
            return null;
        }
        reader.beginObject();

        TagBatch<T> object = new TagBatch<T>();
        while (reader.hasNext()) {
            String name = reader.nextName();
            com.google.gson.stream.JsonToken jsonToken = reader.peek();
            if (jsonToken == com.google.gson.stream.JsonToken.NULL) {
                reader.skipValue();
                continue;
            }
            switch (name) {
                case "tag":
                    object.tag = batchingTypeAdapterFactory.getTagTypeAdapter().read(reader);
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