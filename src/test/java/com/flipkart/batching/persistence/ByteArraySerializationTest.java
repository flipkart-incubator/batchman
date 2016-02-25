package com.flipkart.batching.persistence;

import com.flipkart.Utils;
import com.flipkart.batching.Batch;
import com.flipkart.batching.Data;
import com.flipkart.batching.data.EventData;
import com.flipkart.batching.data.Tag;
import com.flipkart.batching.exception.DeserializeException;
import com.flipkart.batching.exception.SerializeException;

import junit.framework.Assert;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by anirudh.r on 12/02/16.
 */
public class ByteArraySerializationTest<E extends Data,T extends Batch> {
    private SerializationStrategy<E, T> serializationStrategy;
    private Data eventData;

    /**
     * Test the working of ByteArraySerializationStrategy
     *
     * @throws SerializeException
     * @throws DeserializeException
     */
    @Test
    public void testByteArraySerialization() throws SerializeException, DeserializeException {
        serializationStrategy = new ByteArraySerializationStrategy<>();
        eventData = new EventData("Event 1");
        byte[] serializedData = serializationStrategy.serializeData((E) eventData);
        Data data = serializationStrategy.deserializeData(serializedData);
        Assert.assertEquals(eventData, data);
    }

    /**
     * Test if Collection is getting serialized and deserialized
     *
     * @throws SerializeException
     * @throws DeserializeException
     */
    @Test
    public void testCollectionSerialization() throws SerializeException, DeserializeException {
        serializationStrategy = new ByteArraySerializationStrategy<>();
        ArrayList<Data> arrayList = Utils.fakeCollection(4);
        byte[] serializedData = serializationStrategy.serializeCollection((Collection<E>) arrayList);
        Collection<Data> data = (Collection<Data>) serializationStrategy.deserializeCollection(serializedData);
        Assert.assertEquals(arrayList, data);
    }

    @Test
    public void testBatchInfoSerialization() throws DeserializeException, SerializeException {
        serializationStrategy = new ByteArraySerializationStrategy<>();
        Batch<Data> batch = new Batch<>(Utils.fakeCollection(4));
        byte[] serializedData = serializationStrategy.serializeBatch((T) batch);
        Batch data = serializationStrategy.deserializeBatch(serializedData);
        Assert.assertEquals(batch, data);
    }

    /**
     * Test to verify {@link DeserializeException} is thrown.
     *
     * @throws SerializeException
     * @throws DeserializeException
     */
    @Test(expected = DeserializeException.class)
    public void testIfDeserializeExceptionThrown() throws SerializeException, DeserializeException {
        serializationStrategy = new ByteArraySerializationStrategy<>();
        eventData = new EventData("Event 1");
        byte[] serializedData = serializationStrategy.serializeData((E) eventData);
        try {
            String foo = new String(serializedData, "UTF-8");
            foo += "a";
            serializedData = foo.getBytes();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        serializationStrategy.deserializeData(serializedData);
    }

    @Test
    public void testRegisterBatchInfoType() {
        serializationStrategy = new ByteArraySerializationStrategy<>();
        serializationStrategy.registerBatch((Class<T>) Batch.class);
    }

    @Test
    public void testRegisterDataType() {
        serializationStrategy = new ByteArraySerializationStrategy<>();
        serializationStrategy.registerDataType((Class<E>) Data.class);
    }

    @Test
    public void testBuild() {
        serializationStrategy = new ByteArraySerializationStrategy<>();
        serializationStrategy.build();
    }

    /**
     * Test the working of {@link ByteArraySerializationStrategy} for Custom Data
     *
     * @throws SerializeException
     * @throws DeserializeException
     */
    @Test
    public void testByteArraySerializationForCustomData() throws SerializeException, DeserializeException {
        //test to serialize hashmap
        ByteArraySerializationStrategy<Data, Batch<Data>> serializationStrategy = new ByteArraySerializationStrategy<>();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("key", "value");
        Data customData = new CustomData(hashMap);
        byte[] serializedData = serializationStrategy.serializeData(customData);
        Data data = (Data) serializationStrategy.deserializeData(serializedData);
        Assert.assertEquals(customData, data);
        //test to serialize arraylist
        serializationStrategy = new ByteArraySerializationStrategy<>();
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("Event1");
        arrayList.add("Event2");
        arrayList.add("Event3");
        customData = new CustomData(arrayList);
        serializedData = serializationStrategy.serializeData(customData);
        data = serializationStrategy.deserializeData(serializedData);
        Assert.assertEquals(customData, data);
    }

    private static class CustomData extends Data {
        /**
         * Constructor for Data object. This constructor takes {@link Tag} and {@link Object} as
         * parameter and generates an eventId = (System.currentTimeMillis() + System.nanoTime())
         *
         * @param data data object
         */
        public CustomData(HashMap<String, Object> data) {
            super(data);
        }

        public CustomData(ArrayList<String> strings) {
            super(strings);
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof CustomData) {
                return ((CustomData) o).getEventId() == getEventId() && ((CustomData) o).getData().equals(getData());
            } else {
                return super.equals(o);
            }
        }
    }
}
