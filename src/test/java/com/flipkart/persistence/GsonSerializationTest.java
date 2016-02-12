package com.flipkart.persistence;

import com.flipkart.data.Data;
import com.flipkart.data.EventData;
import com.flipkart.data.Tag;
import com.flipkart.exception.DeserializeException;
import com.flipkart.exception.SerializeException;
import com.google.gson.JsonSyntaxException;

import junit.framework.Assert;

import org.junit.Test;

import java.io.UnsupportedEncodingException;

/**
 * Created by anirudh.r on 12/02/16.
 */
public class GsonSerializationTest {

    private SerializationStrategy serializationStrategy;
    private Data eventData;

    /**
     * Test the working of GSONSerializationStrategy
     *
     * @throws SerializeException
     * @throws DeserializeException
     */
    @Test
    public void testGSONSerialization() throws SerializeException, DeserializeException {
        serializationStrategy = new GsonSerializationStrategy();
        eventData = new EventData(new Tag("u1"), "Event 1");
        byte[] serializedData = serializationStrategy.serialize(eventData);
        Data data = (Data) serializationStrategy.deserialize(serializedData);
        Assert.assertEquals(eventData, data);
    }

    /**
     * Test to verify {@link JsonSyntaxException} is thrown.
     *
     * @throws SerializeException
     * @throws DeserializeException
     */
    @Test(expected = JsonSyntaxException.class)
    public void testIfDeserializeExceptionThrown() throws SerializeException, DeserializeException {
        serializationStrategy = new GsonSerializationStrategy();
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
}
