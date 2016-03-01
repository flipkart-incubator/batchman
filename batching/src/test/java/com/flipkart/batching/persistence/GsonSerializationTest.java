package com.flipkart.batching.persistence;

import com.flipkart.Utils;
import com.flipkart.batching.Batch;
import com.flipkart.batching.BatchManager;
import com.flipkart.batching.Data;
import com.flipkart.batching.data.EventData;
import com.flipkart.batching.data.Tag;
import com.flipkart.batching.exception.DeserializeException;
import com.flipkart.batching.exception.SerializeException;
import com.google.gson.JsonSyntaxException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by anirudh.r on 12/02/16.
 */
public class GsonSerializationTest {

    @Mock
    PersistenceStrategy<Data> inMemoryPersistenceStrategy;
    private GsonSerializationStrategy<Data, Batch<Data>> serializationStrategy;
    private Batch<Data> batch;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        serializationStrategy = new GsonSerializationStrategy<>();
        BatchManager.registerBuiltInTypes(serializationStrategy);
        serializationStrategy.build();
        ArrayList<Data> dataCollection = Utils.fakeCollection(4);
        batch = new Batch<>(dataCollection);
    }

    /**
     * Test the working of GSONSerializationStrategy
     *
     * @throws SerializeException
     * @throws DeserializeException
     */
    @Test
    public void testGSONSerialization() {
        byte[] serializedData;
        try {
            Batch<Data> batch = new Batch<>(Utils.fakeCollection(3));
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
     * Test to verify {@link JsonSyntaxException} is thrown.
     *
     * @throws SerializeException
     * @throws DeserializeException
     */
    @Test(expected = Exception.class)
    public void testIfExceptionThrownWhenCorrupt() throws SerializeException, DeserializeException {
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

    @Test
    public void testCollectionSerialization() {
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

    @Test(expected = IllegalStateException.class)
    public void testIfBuildNotCalled() {
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
        BatchManager.registerBuiltInTypes(serializationStrategy);
        serializationStrategy.build();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("key", "value");
        Data eventData = new EventData(hashMap);
        byte[] serializedData = serializationStrategy.serializeData(eventData);
        Data data = serializationStrategy.deserializeData(serializedData);
        Assert.assertEquals(eventData, data);
        //test to serialize arraylist

        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("Event1");
        arrayList.add("Event2");
        arrayList.add("Event3");
        eventData = new EventData(arrayList);
        serializedData = serializationStrategy.serializeData(eventData);
        data = serializationStrategy.deserializeData(serializedData);
        Assert.assertEquals(eventData, data);
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
