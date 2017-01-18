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

import com.flipkart.batching.core.Data;
import com.flipkart.batching.core.DataCollection;
import com.flipkart.batching.core.batch.SizeBatch;
import com.flipkart.batching.gson.adapters.BatchingTypeAdapters;
import com.flipkart.batching.gson.adapters.DataCollectionTypeAdapter;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public final class SizeBatchTypeAdapter<T extends Data> extends TypeAdapter<SizeBatch<T>> {
    private final TypeAdapter<DataCollection<T>> typeAdapter;

    public SizeBatchTypeAdapter(TypeAdapter<T> typeAdapter) {
        this.typeAdapter = new DataCollectionTypeAdapter<T>(typeAdapter);
    }

    @Override
    public void write(JsonWriter writer, SizeBatch<T> object) throws IOException {
        writer.beginObject();
        if (object == null) {
            writer.endObject();
            return;
        }

        writer.name("maxBatchSize");
        writer.value(object.getMaxBatchSize());

        if (object.dataCollection != null) {
            writer.name("dataCollection");
            typeAdapter.write(writer, object.dataCollection);
        }

        writer.endObject();
    }

    @Override
    public SizeBatch<T> read(JsonReader reader) throws IOException {
        if (reader.peek() == com.google.gson.stream.JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        if (reader.peek() != com.google.gson.stream.JsonToken.BEGIN_OBJECT) {
            reader.skipValue();
            return null;
        }
        reader.beginObject();

        int maxBatchSize = 0;
        DataCollection<T> dataCollection = null;

        while (reader.hasNext()) {
            String name = reader.nextName();
            com.google.gson.stream.JsonToken jsonToken = reader.peek();
            if (jsonToken == com.google.gson.stream.JsonToken.NULL) {
                reader.skipValue();
                continue;
            }

            switch (name) {
                case "maxBatchSize":
                    maxBatchSize = BatchingTypeAdapters.INTEGER.read(reader);
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
        return dataCollection == null ? null : new SizeBatch<>(dataCollection.dataCollection, maxBatchSize);
    }
}