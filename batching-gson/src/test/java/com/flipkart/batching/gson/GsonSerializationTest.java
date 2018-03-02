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

import com.flipkart.batching.core.Batch;
import com.flipkart.batching.core.BatchImpl;
import com.flipkart.batching.core.Data;
import com.flipkart.batching.core.batch.TagBatch;
import com.flipkart.batching.core.data.EventData;
import com.flipkart.batching.core.data.Tag;
import com.google.gson.JsonSyntaxException;

import junit.framework.Assert;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 * Test for {@link GsonSerializationStrategy}
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class GsonSerializationTest {

    /**
     * Test the working of {@link GsonSerializationStrategy#serializeBatch(Batch)}
     * and {@link GsonSerializationStrategy#deserializeBatch(byte[])}
     */
    @Test
    public void testGSONSerialization() {
        GsonSerializationStrategy<Data, BatchImpl<Data>> serializationStrategy = new GsonSerializationStrategy<>();
        serializationStrategy.build();

        byte[] serializedData;
        try {
            BatchImpl<Data> batch = new BatchImpl<>(Utils.fakeCollection(3));
            serializedData = serializationStrategy.serializeBatch(batch);
            Batch batchReturned = (Batch) serializationStrategy.deserializeBatch(serializedData);
            Assert.assertEquals(batch.getClass(), batchReturned.getClass());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test to verify {@link JsonSyntaxException} is thrown, when the byte to be deserialized gets corrupted
     *
     * @throws IOException
     */
    @Test
    public void testIfExceptionThrownWhenCorrupt() throws IOException {
        GsonSerializationStrategy<EventData, BatchImpl<Data>> serializationStrategy = new GsonSerializationStrategy<>();
        serializationStrategy.build();

        ArrayList<Data> dataCollection = Utils.fakeCollection(4);
        BatchImpl<Data> batch = new BatchImpl<>(dataCollection);

        byte[] serializedData = serializationStrategy.serializeBatch(batch);
        try {
            String foo = new String(serializedData, "UTF-8");
            foo += "a";
            serializedData = foo.getBytes();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        BatchImpl<Data> dataBatch = serializationStrategy.deserializeBatch(serializedData);
        Assert.assertTrue(dataBatch != batch);
    }

    /**
     * Test the working of {@link GsonSerializationStrategy#serializeCollection(Collection)}
     * and {@link GsonSerializationStrategy#deserializeCollection(byte[])}
     */
    @Test
    public void testCollectionSerialization() {
        GsonSerializationStrategy<Data, Batch<Data>> serializationStrategy;
        ArrayList<Data> dataCollection = Utils.fakeCollection(4);
        serializationStrategy = new GsonSerializationStrategy<>();
        serializationStrategy.build();
        Collection<Data> fakeCollection = Utils.fakeCollection(4);

        byte[] serializedData;
        try {
            serializedData = serializationStrategy.serializeCollection(fakeCollection);
            Collection<Data> collectionReturned = serializationStrategy.deserializeCollection(serializedData);//todo not deserialize collection
            Assert.assertEquals(fakeCollection, collectionReturned);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test to verify that {@link IllegalStateException} gets thrown when {@link GsonSerializationStrategy#build()} is not called
     */
    @Test(expected = IllegalStateException.class)
    public void testIfBuildNotCalled() {
        GsonSerializationStrategy<Data, Batch<Data>> serializationStrategy;
        ArrayList<Data> dataCollection = Utils.fakeCollection(4);
        serializationStrategy = new GsonSerializationStrategy<>();
        serializationStrategy.build();
        Batch<Data> batch = new BatchImpl<>(dataCollection);

        serializationStrategy = new GsonSerializationStrategy<>();
        ArrayList<Data> fakeCollection = Utils.fakeCollection(4);
        try {
            serializationStrategy.serializeCollection(fakeCollection);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test the working of {@link GsonSerializationStrategy} for Custom Data
     *
     * @throws IOException
     */
    @Test
    public void testGSONSerializationForData() throws IOException {
        //test to serialize hashmap
        GsonSerializationStrategy<Data, Batch<Data>> serializationStrategy = new GsonSerializationStrategy<>();
        serializationStrategy.build();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("key", "value");
        Data eventData = new EventData();
        byte[] serializedData = serializationStrategy.serializeData(eventData);
        Data data = serializationStrategy.deserializeData(serializedData);
        Assert.assertEquals(eventData, data);

        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("Event1");
        arrayList.add("Event2");
        arrayList.add("Event3");
        eventData = new EventData();
        serializedData = serializationStrategy.serializeData(eventData);
        data = serializationStrategy.deserializeData(serializedData);
        Assert.assertEquals(eventData, data);
    }

    @Test
    public void testTagBatchWithSubType() throws Exception {

        Tag tag = new Tag("demo");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key", "value");
        Utils.CustomTagData customTagData = new Utils.CustomTagData(tag, jsonObject);

        GsonSerializationStrategy<Utils.CustomTagData, TagBatch<Utils.CustomTagData>> gsonSerializationStrategy =
                new GsonSerializationStrategy<>();
        gsonSerializationStrategy.registerDataSubTypeAdapters(Utils.CustomTagData.class, new Utils.CustomTagDataAdapter());
        gsonSerializationStrategy.build();

        TagBatch<Utils.CustomTagData> tagBatch = new TagBatch<>(tag, new BatchImpl<>(Collections.singleton(customTagData)));

        byte[] bytes = gsonSerializationStrategy.serializeBatch(tagBatch);
        TagBatch<Utils.CustomTagData> customTagDataTagBatch = gsonSerializationStrategy.deserializeBatch(bytes);

        Collection<Utils.CustomTagData> dataCollection = customTagDataTagBatch.getDataCollection();
        Collection<Utils.CustomTagData> dataCollection1 = tagBatch.getDataCollection();
        org.junit.Assert.assertTrue(dataCollection.size() == dataCollection1.size());
    }
}