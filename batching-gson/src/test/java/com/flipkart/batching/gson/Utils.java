/*
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2017 Flipkart Internet Pvt. Ltd.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package com.flipkart.batching.gson;

import com.flipkart.batching.core.Data;
import com.flipkart.batching.core.data.EventData;
import com.flipkart.batching.core.data.Tag;
import com.flipkart.batching.core.data.TagData;
import com.flipkart.batching.gson.adapters.BatchingTypeAdapters;
import com.flipkart.batching.gson.adapters.data.TagTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Utils class for Test
 */
public class Utils {

    /**
     * Method to create fake array list of Data.
     *
     * @param size
     * @return dataList
     */
    private static Data eventData;

    public static ArrayList<Data> fakeCollection(int size) {
        ArrayList<Data> dataList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            eventData = new EventData();
            eventData.setEventId(System.currentTimeMillis() + System.nanoTime() + i);
            dataList.add(eventData);
        }
        return dataList;
    }

    public static class CustomTagData extends TagData {
        @SerializedName("event")
        public JSONObject event;

        public CustomTagData(Tag tag, JSONObject event) {
            super(tag);
            this.event = event;
        }

        public JSONObject getEvent() {
            return event;
        }

        @Override
        public String toString() {
            return super.toString() + ":" + getEventId();
        }
    }

    public static class CustomTagDataAdapter extends TypeAdapter<CustomTagData> {

        private final Gson gson = new Gson();
        private TypeAdapter<Tag> tagTypeAdapter;

        public CustomTagDataAdapter() {
            this.tagTypeAdapter = new TagTypeAdapter();
        }

        @Override
        public void write(JsonWriter writer, CustomTagData object) throws IOException {
            writer.beginObject();
            if (object == null) {
                writer.endObject();
                return;
            }

            if (object.getTag() != null) {
                writer.name("tag");
                tagTypeAdapter.write(writer, object.getTag());
            }

            if (object.event != null) {
                writer.name("event");
                BatchingTypeAdapters.getJSONObjectTypeAdapter(gson).write(writer, object.event);
            }

            writer.name("eventId");
            writer.value(object.getEventId());

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

            Tag tag = null;
            Long eventId = 0L;
            JSONObject event = null;
            while (reader.hasNext()) {
                String name = reader.nextName();
                com.google.gson.stream.JsonToken jsonToken = reader.peek();
                if (jsonToken == com.google.gson.stream.JsonToken.NULL) {
                    reader.skipValue();
                    continue;
                }
                switch (name) {
                    case "tag":
                        tag = tagTypeAdapter.read(reader);
                        break;
                    case "eventId":
                        eventId = BatchingTypeAdapters.LONG.read(reader);
                        break;
                    case "event":
                        event = BatchingTypeAdapters.getJSONObjectTypeAdapter(gson).read(reader);
                        break;
                    default:
                        reader.skipValue();
                        break;
                }
            }

            reader.endObject();
            CustomTagData customTagData = new CustomTagData(tag, event);
            customTagData.setEventId(eventId);
            return customTagData;
        }
    }
}