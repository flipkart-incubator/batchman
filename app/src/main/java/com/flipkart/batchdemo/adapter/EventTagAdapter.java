package com.flipkart.batchdemo.adapter;

import com.flipkart.batchdemo.EventTag;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public final class EventTagAdapter extends TypeAdapter<EventTag> {
    public EventTagAdapter() {
    }

    @Override
    public void write(JsonWriter writer, EventTag object) throws IOException {
        writer.beginObject();
        if (object == null) {
            writer.endObject();
            return;
        }

        if (object.id != null) {
            writer.name("id");
            com.google.gson.internal.bind.TypeAdapters.STRING.write(writer, object.id);
        }

        if (object.url != null) {
            writer.name("id");
            com.google.gson.internal.bind.TypeAdapters.STRING.write(writer, object.url);
        }

        writer.endObject();
    }

    @Override
    public EventTag read(JsonReader reader) throws IOException {
        if (reader.peek() == com.google.gson.stream.JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        if (reader.peek() != com.google.gson.stream.JsonToken.BEGIN_OBJECT) {
            reader.skipValue();
            return null;
        }
        reader.beginObject();

        EventTag object = new EventTag();
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
                case "url":
                    object.url = com.google.gson.internal.bind.TypeAdapters.STRING.read(reader);
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