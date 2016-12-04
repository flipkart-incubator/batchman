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

package com.flipkart.batching.gson;

import com.flipkart.batching.core.Batch;
import com.flipkart.batching.core.BatchImpl;
import com.flipkart.batching.core.Data;
import com.flipkart.batching.core.SerializationStrategy;
import com.flipkart.batching.core.batch.SizeBatch;
import com.flipkart.batching.core.batch.SizeTimeBatch;
import com.flipkart.batching.core.batch.TagBatch;
import com.flipkart.batching.core.batch.TimeBatch;
import com.flipkart.batching.core.data.EventData;
import com.flipkart.batching.core.data.TagData;
import com.flipkart.batching.core.exception.DeserializeException;
import com.flipkart.batching.core.exception.SerializeException;
import com.google.gson.JsonSyntaxException;

import junit.framework.Assert;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 * Test for {@link GsonSerializationStrategy}
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class GsonSerializationTest {

    @Test
    public void testGSONSerializationForDataCollection() throws Exception {
        GsonSerializationStrategy<Data, Batch<Data>> serializationStrategy;
        serializationStrategy = new GsonSerializationStrategy<>();
        registerBuiltInTypes(serializationStrategy);
        serializationStrategy.build();

        String inputStream = "{\"data\":[{\"listingId\":\"1\",\"productId\":\"dfg\",\"requestId\":\"fgh\",\"timestamp\":1480826105537},{\"listingId\":\"1\",\"productId\":\"dfg\",\"requestId\":\"fgh\",\"timestamp\":1480826105537},{\"listingId\":\"1\",\"productId\":\"dfg\",\"requestId\":\"fgh\",\"timestamp\":1480826105537},{\"listingId\":\"1\",\"productId\":\"dfg\",\"requestId\":\"fgh\",\"timestamp\":1480826105537},{\"listingId\":\"1\",\"productId\":\"dfg\",\"requestId\":\"fgh\",\"timestamp\":1480826105537}]}";

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", "Anirudh");
        Batch<Data> originalBatch = new BatchImpl(Collections.singleton(jsonObject.toString()));

        byte[] bytes = serializationStrategy.serializeBatch(originalBatch);
        Batch<Data> deserializedBatch = serializationStrategy.deserializeBatch(bytes);

        for (Object data : originalBatch.getDataCollection()) {
            System.out.println(data);
        }
        for (Object data : deserializedBatch.getDataCollection()) {
            System.out.println(data);
        }
        Assert.assertEquals(deserializedBatch.getDataCollection().size(), originalBatch.getDataCollection().size());
    }

    /**
     * Test the working of {@link GsonSerializationStrategy#serializeBatch(Batch)}
     * and {@link GsonSerializationStrategy#deserializeBatch(byte[])}
     */
    @Test
    public void testGSONSerialization() {
        GsonSerializationStrategy<Data, Batch<Data>> serializationStrategy;
        serializationStrategy = new GsonSerializationStrategy<>();
        registerBuiltInTypes(serializationStrategy);
        serializationStrategy.build();
        ArrayList<Data> dataCollection = Utils.fakeCollection(4);

        byte[] serializedData;
        try {
            Batch<Data> batch = new BatchImpl<>(Utils.fakeCollection(3));
            serializedData = serializationStrategy.serializeBatch(batch);
            Batch batchReturned = (Batch) serializationStrategy.deserializeBatch(serializedData);
            Assert.assertEquals(batch.getClass(), batchReturned.getClass());
        } catch (SerializeException e) {
            e.getRealException().printStackTrace();
        } catch (DeserializeException e) {
            e.getRealException().printStackTrace();
        }
    }

    /**
     * Test to verify {@link JsonSyntaxException} is thrown, when the byte to be deserialized gets corrupted
     *
     * @throws SerializeException
     * @throws DeserializeException
     */
    @Test(expected = DeserializeException.class)
    public void testIfExceptionThrownWhenCorrupt() throws SerializeException, DeserializeException {
        GsonSerializationStrategy<Data, Batch<Data>> serializationStrategy;
        ArrayList<Data> dataCollection = Utils.fakeCollection(4);
        serializationStrategy = new GsonSerializationStrategy<>();
        registerBuiltInTypes(serializationStrategy);
        serializationStrategy.build();
        Batch<Data> batch = new BatchImpl<>(dataCollection);

        byte[] serializedData = serializationStrategy.serializeBatch(batch);
        try {
            String foo = new String(serializedData, "UTF-8");
            foo += "a";
            serializedData = foo.getBytes();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        serializationStrategy.deserializeData(serializedData);
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
        registerBuiltInTypes(serializationStrategy);
        serializationStrategy.build();
        Collection<Data> fakeCollection = Utils.fakeCollection(4);

        byte[] serializedData;
        try {
            serializedData = serializationStrategy.serializeCollection(fakeCollection);
            Collection<Data> collectionReturned = serializationStrategy.deserializeCollection(serializedData);//todo not deserialize collection
            Assert.assertEquals(fakeCollection, collectionReturned);
        } catch (SerializeException e) {
            e.getRealException().printStackTrace();
        } catch (DeserializeException e) {
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
        registerBuiltInTypes(serializationStrategy);
        serializationStrategy.build();
        Batch<Data> batch = new BatchImpl<>(dataCollection);

        serializationStrategy = new GsonSerializationStrategy<>();
        ArrayList<Data> fakeCollection = Utils.fakeCollection(4);
        try {
            serializationStrategy.serializeCollection(fakeCollection);
        } catch (SerializeException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test the working of {@link GsonSerializationStrategy} for Custom Data
     *
     * @throws SerializeException
     * @throws DeserializeException
     */
    @Test
    public void testGSONSerializationForData() throws SerializeException, DeserializeException {
        //test to serialize hashmap
        GsonSerializationStrategy<Data, Batch<Data>> serializationStrategy = new GsonSerializationStrategy<>();
        serializationStrategy.registerDataType(Data.class);
        registerBuiltInTypes(serializationStrategy);
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

    private void registerBuiltInTypes(SerializationStrategy serializationStrategy) {
        serializationStrategy.registerDataType(TagData.class);
        serializationStrategy.registerBatch(BatchImpl.class);
        serializationStrategy.registerDataType(EventData.class);
        serializationStrategy.registerBatch(SizeBatch.class);
        serializationStrategy.registerBatch(TimeBatch.class);
        serializationStrategy.registerBatch(TagBatch.class);
        serializationStrategy.registerBatch(SizeTimeBatch.class);
    }
}
