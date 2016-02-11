package com.flipkart.persistence;

import com.flipkart.data.Data;
import com.flipkart.data.EventData;
import com.flipkart.data.Tag;
import com.flipkart.exception.DeserializeException;
import com.flipkart.exception.SerializeException;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Created by anirudh.r on 11/02/16.
 */
public class SerializationTest {

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

        Assert.assertEquals(eventData.getData(), data.getData());
    }

    /**
     * Test the working of GsonSerializationStrategy
     *
     * @throws SerializeException
     * @throws DeserializeException
     */
    @Test
    public void testGsonSerialization() throws SerializeException, DeserializeException {

        serializationStrategy = new GsonSerializationStrategy();
        eventData = new EventData(new Tag("u1"), "Event 1");
        byte[] serializedData = serializationStrategy.serialize(eventData);
        Data data = (Data) serializationStrategy.deserialize(serializedData);
        Assert.assertEquals(eventData.getData(), data.getData());

    }
}
