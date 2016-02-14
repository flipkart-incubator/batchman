package com.flipkart.persistence;

import com.flipkart.data.Data;
import com.flipkart.data.EventData;
import com.flipkart.data.Tag;
import com.flipkart.exception.DeserializeException;
import com.flipkart.exception.SerializeException;

import junit.framework.Assert;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by anirudh.r on 12/02/16.
 */
public class ByteArraySerializationTest {
    private SerializationStrategy serializationStrategy;
    private Data eventData;

    /**
     * Test the working of ByteArraySerializationStrategy
     *
     * @throws SerializeException
     * @throws DeserializeException
     */
    @Test
    public void testByteArraySerialization() throws SerializeException, DeserializeException {
        serializationStrategy = new ByteArraySerializationStrategy();
        eventData = new EventData(new Tag("u1"), "Event 1");
        byte[] serializedData = serializationStrategy.serialize(eventData);
        Data data = (Data) serializationStrategy.deserialize(serializedData);
        Assert.assertEquals(eventData, data);
    }

    /**
     * Test to verify {@link DeserializeException} is thrown.
     *
     * @throws SerializeException
     * @throws DeserializeException
     */
    @Test(expected = DeserializeException.class)
    public void testIfDeserializeExceptionThrown() throws SerializeException, DeserializeException {
        serializationStrategy = new ByteArraySerializationStrategy();
        eventData = new EventData(new Tag("u1"), "Event 1");
        byte[] serializedData = serializationStrategy.serialize(eventData);
        try {
            String foo = new String(serializedData, "UTF-8");
            foo += "a";
            serializedData = foo.getBytes();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        serializationStrategy.deserialize(serializedData);
    }

    private class CustomData extends Data {
        HashMap<String, String> stringHashMap;
        ArrayList<String> stringArrayList;

        /**
         * Constructor for Data object. This constructor takes {@link Tag} and {@link Object} as
         * parameter and generates an eventId = (System.currentTimeMillis() + System.nanoTime())
         *
         * @param tag  tag associated with data
         * @param data data object
         */
        public CustomData(Tag tag, HashMap<String, String> data) {
            super(tag, data);
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof CustomData) {
                return ((CustomData) o).getTag().equals(getTag()) && ((CustomData) o).getEventId() == getEventId() && ((CustomData) o).getData().equals(getData());
            } else {
                return super.equals(o);
            }
        }
    }

//    @Test
//    public void testByteArraySerializationForCustomData() throws SerializeException, DeserializeException {
//        serializationStrategy = new ByteArraySerializationStrategy();
//        HashMap<String, String> hashMap = new HashMap<>();
//        hashMap.put("key", "value");
//        ArrayList<String> arrayList = new ArrayList<>();
//        arrayList.add("value1");
//
//        Data eventData = new CustomData(new Tag("u1"), hashMap);
//        byte[] serializedData = serializationStrategy.serialize(eventData);
//        Data data = (Data) serializationStrategy.deserialize(serializedData);
//        Assert.assertEquals(eventData, data);
//    }
}
