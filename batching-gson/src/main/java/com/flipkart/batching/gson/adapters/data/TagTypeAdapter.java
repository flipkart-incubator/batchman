package com.flipkart.batching.gson.adapters.data;

import com.flipkart.batching.core.data.Tag;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public final class TagTypeAdapter extends TypeAdapter<Tag> {
    public TagTypeAdapter() {
    }

    @Override
    public void write(JsonWriter writer, Tag object) throws IOException {
        writer.beginObject();
        if (object == null) {
            writer.endObject();
            return;
        }

        if (object.id != null) {
            writer.name("id");
            com.google.gson.internal.bind.TypeAdapters.STRING.write(writer, object.id);
        }

        writer.endObject();
    }

    @Override
    public Tag read(JsonReader reader) throws IOException {
        if (reader.peek() == com.google.gson.stream.JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        if (reader.peek() != com.google.gson.stream.JsonToken.BEGIN_OBJECT) {
            reader.skipValue();
            return null;
        }
        reader.beginObject();

        Tag object = new Tag();
        while (reader.hasNext()) {
            String name = reader.nextName();
            com.google.gson.stream.JsonToken jsonToken = reader.peek();
            if (jsonToken == com.google.gson.stream.JsonToken.NULL) {
                reader.skipValue();
                continue;
            }
            switch (name) {
                case "id":
                    object.id = com.google.gson.internal.bind.TypeAdapters.STRING.read(reader);
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