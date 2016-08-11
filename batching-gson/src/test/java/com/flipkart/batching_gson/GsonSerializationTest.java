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

package com.flipkart.batching_gson;

import com.flipkart.batching_gson.utils.GsonSerializationStrategy;
import com.flipkart.batchingcore.Batch;
import com.flipkart.batchingcore.BatchImpl;
import com.flipkart.batchingcore.Data;
import com.flipkart.batchingcore.SerializationStrategy;
import com.flipkart.batchingcore.batch.SizeBatch;
import com.flipkart.batchingcore.batch.SizeTimeBatch;
import com.flipkart.batchingcore.batch.TagBatch;
import com.flipkart.batchingcore.batch.TimeBatch;
import com.flipkart.batchingcore.data.EventData;
import com.flipkart.batchingcore.data.TagData;
import com.flipkart.batchingcore.exception.DeserializeException;
import com.flipkart.batchingcore.exception.SerializeException;
import com.google.gson.JsonSyntaxException;

import junit.framework.Assert;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Test for {@link GsonSerializationStrategy}
 */
public class GsonSerializationTest {

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
