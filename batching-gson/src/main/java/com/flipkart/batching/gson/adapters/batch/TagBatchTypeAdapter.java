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
package com.flipkart.batching.gson.adapters.batch;

import com.flipkart.batching.core.BatchImpl;
import com.flipkart.batching.core.DataCollection;
import com.flipkart.batching.core.batch.TagBatch;
import com.flipkart.batching.core.data.Tag;
import com.flipkart.batching.core.data.TagData;
import com.flipkart.batching.gson.adapters.DataCollectionTypeAdapter;
import com.flipkart.batching.gson.adapters.data.TagTypeAdapter;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public final class TagBatchTypeAdapter<T extends TagData> extends TypeAdapter<TagBatch<T>> {
    private final TypeAdapter<DataCollection<T>> typeAdapter;
    private final TypeAdapter<Tag> tagTypeAdapter;

    public TagBatchTypeAdapter(TypeAdapter<T> typeAdapter) {
        this.typeAdapter = new DataCollectionTypeAdapter<>(typeAdapter);
        this.tagTypeAdapter = new TagTypeAdapter();
    }

    @Override
    public void write(JsonWriter writer, TagBatch<T> object) throws IOException {
        writer.beginObject();
        if (object == null) {
            writer.endObject();
            return;
        }

        if (object.getTag() != null) {
            writer.name("tag");
            tagTypeAdapter.write(writer, object.getTag());
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

        Tag tag = null;
        DataCollection<T> collection = null;

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
                case "dataCollection":
                    collection = typeAdapter.read(reader);
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }

        reader.endObject();
        return collection == null || tag == null ? null : new TagBatch<>(tag, new BatchImpl<>(collection.dataCollection));
    }
}