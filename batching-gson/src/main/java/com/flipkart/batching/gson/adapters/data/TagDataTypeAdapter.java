package com.flipkart.batching.gson.adapters.data;

import com.flipkart.batching.core.data.Tag;
import com.flipkart.batching.core.data.TagData;
import com.flipkart.batching.gson.adapters.BatchingTypeAdapters;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public final class TagDataTypeAdapter extends TypeAdapter<TagData> {

    private TypeAdapter<Tag> tagTypeAdapter;

    public TagDataTypeAdapter() {
        this.tagTypeAdapter = new TagTypeAdapter();
    }

    @Override
    public void write(JsonWriter writer, TagData object) throws IOException {
        writer.beginObject();
        if (object == null) {
            writer.endObject();
            return;
        }

        if (object.tag != null) {
            writer.name("tag");
            tagTypeAdapter.write(writer, object.tag);
        }

        writer.name("eventId");
        writer.value(object.eventId);

        writer.endObject();
    }

    @Override
    public TagData read(JsonReader reader) throws IOException {
        if (reader.peek() == com.google.gson.stream.JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        if (reader.peek() != com.google.gson.stream.JsonToken.BEGIN_OBJECT) {
            reader.skipValue();
            return null;
        }
        reader.beginObject();

        TagData object = new TagData();
        while (reader.hasNext()) {
            String name = reader.nextName();
            com.google.gson.stream.JsonToken jsonToken = reader.peek();
            if (jsonToken == com.google.gson.stream.JsonToken.NULL) {
                reader.skipValue();
                continue;
            }
            switch (name) {
                case "tag":
                    object.tag = tagTypeAdapter.read(reader);
                    break;
                case "eventId":
                    object.eventId = BatchingTypeAdapters.LONG.read(reader);
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