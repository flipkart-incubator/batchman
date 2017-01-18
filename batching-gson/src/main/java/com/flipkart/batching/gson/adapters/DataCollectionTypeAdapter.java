/*
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2016 Flipkart Internet Pvt. Ltd.
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
package com.flipkart.batching.gson.adapters;

import android.support.annotation.NonNull;

import com.flipkart.batching.core.Data;
import com.flipkart.batching.core.DataCollection;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class DataCollectionTypeAdapter<T extends Data> extends TypeAdapter<DataCollection<T>> {

    @NonNull
    private TypeAdapter<Collection<T>> collectionTypeAdapter;

    public DataCollectionTypeAdapter(@NonNull TypeAdapter<T> typeAdapter) {
        collectionTypeAdapter = new BatchingTypeAdapters.ListTypeAdapter<>(typeAdapter, new ObjectConstructor<Collection<T>>() {
            @Override
            public Collection<T> construct() {
                return new ArrayList<>();
            }
        });
    }

    @Override
    public void write(JsonWriter out, DataCollection<T> value) throws IOException {
        out.beginObject();
        if (value == null) {
            out.endObject();
            return;
        }

        if (value.dataCollection != null) {
            out.name("dataCollection");
            collectionTypeAdapter.write(out, value.dataCollection);
        }

        out.endObject();
    }

    @Override
    public DataCollection<T> read(JsonReader reader) throws IOException {
        if (reader.peek() == com.google.gson.stream.JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        if (reader.peek() != com.google.gson.stream.JsonToken.BEGIN_OBJECT) {
            reader.skipValue();
            return null;
        }
        reader.beginObject();

        Collection<T> collection = null;
        while (reader.hasNext()) {
            String name = reader.nextName();
            com.google.gson.stream.JsonToken jsonToken = reader.peek();
            if (jsonToken == com.google.gson.stream.JsonToken.NULL) {
                reader.skipValue();
                continue;
            }
            switch (name) {
                case "dataCollection":
                    collection = collectionTypeAdapter.read(reader);
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }

        reader.endObject();
        return null != collection ? new DataCollection<>(collection) : null;
    }
}