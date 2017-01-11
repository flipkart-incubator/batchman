package com.flipkart.batchdemo.adapter;

import com.flipkart.batchdemo.CustomTagData;
import com.flipkart.batching.core.data.Tag;
import com.flipkart.batching.gson.adapters.KnownTypeAdapters;
import com.flipkart.batching.gson.adapters.data.TagTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by anirudh.r on 11/01/17.
 */

public class CustomTagDataAdapter extends TypeAdapter<CustomTagData> {

    private TypeAdapter<Tag> tagTypeAdapter;
    private TypeAdapter<JSONObject> jsonObjectTypeAdapter;

    public CustomTagDataAdapter() {
        this.tagTypeAdapter = new TagTypeAdapter();
        this.jsonObjectTypeAdapter = new Gson().getAdapter(new TypeToken<JSONObject>() {
        });
    }

    @Override
    public void write(JsonWriter writer, CustomTagData object) throws IOException {
        writer.beginObject();
        if (object == null) {
            writer.endObject();
            return;
        }

        if (object.tag != null) {
            writer.name("tag");
            tagTypeAdapter.write(writer, object.tag);
        }

        if (object.event != null) {
            writer.name("event");
            jsonObjectTypeAdapter.write(writer, object.event);
        }

        writer.name("eventId");
        writer.value(object.eventId);

        writer.endObject();
    }

    @Override
    public CustomTagData read(JsonReader reader) throws IOException {
        if (reader.peek() == com.google.gson.stream.JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        if (reader.peek() != com.google.gson.stream.JsonToken.BEGIN_OBJECT) {
            reader.skipValue();
            return null;
        }
        reader.beginObject();

        CustomTagData object = new CustomTagData();
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
                    object.eventId = KnownTypeAdapters.LONG.read(reader);
                    break;
                case "event":
                    object.event = jsonObjectTypeAdapter.read(reader);
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