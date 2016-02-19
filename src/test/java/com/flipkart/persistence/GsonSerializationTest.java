package com.flipkart.persistence;

import com.flipkart.Utils;
import com.flipkart.batching.BatchInfo;
import com.flipkart.batching.BatchManager;
import com.flipkart.batching.SizeBatchingStrategy;
import com.flipkart.data.Batch;
import com.flipkart.data.Data;
import com.flipkart.data.Tag;
import com.flipkart.exception.DeserializeException;
import com.flipkart.exception.SerializeException;
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
    PersistenceStrategy inMemoryPersistenceStrategy;
    private SerializationStrategy serializationStrategy;
    private Batch batch;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        serializationStrategy = new GsonSerializationStrategy();
        BatchManager.registerBuiltInTypes(serializationStrategy);
        serializationStrategy.build();
        BatchInfo batchInfo = new SizeBatchingStrategy.SizeBatchInfo(5);
        ArrayList<Data> dataCollection = Utils.fakeCollection(4);

        batch = new Batch(batchInfo, dataCollection);
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
            serializedData = serializationStrategy.serializeBatch(batch);
            Batch batchReturned = (Batch) serializationStrategy.deserializeBatch(serializedData);
            Assert.assertEquals(batch, batchReturned);
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
        ArrayList<Data> fakeCollection = Utils.fakeCollection(4);
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
        serializationStrategy = new GsonSerializationStrategy();
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
    public void testGSONSerializationForCustomData() throws SerializeException, DeserializeException {
        //test to serialize hashmap
        GsonSerializationStrategy serializationStrategy = new GsonSerializationStrategy();
        serializationStrategy.registerDataType(CustomData.class);
        BatchManager.registerBuiltInTypes(serializationStrategy);
        serializationStrategy.build();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("key", "value");
        Data customData = new CustomData(new Tag("u1"), hashMap);
        byte[] serializedData = serializationStrategy.serializeData(customData);
        Data data = serializationStrategy.deserializeData(serializedData);
        Assert.assertEquals(customData, data);
        //test to serialize arraylist

        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("Event1");
        arrayList.add("Event2");
        arrayList.add("Event3");
        customData = new CustomData(new Tag("u1"), arrayList);
        serializedData = serializationStrategy.serializeData(customData);
        data = (Data) serializationStrategy.deserializeData(serializedData);
        Assert.assertEquals(customData, data);
    }

    private static class CustomData extends Data {
        /**
         * Constructor for Data object. This constructor takes {@link Tag} and {@link Object} as
         * parameter and generates an eventId = (System.currentTimeMillis() + System.nanoTime())
         *
         * @param tag  tag associated with data
         * @param data data object
         */

        public CustomData(Tag tag, HashMap<String, Object> data) {
            super(tag, data);
        }

        public CustomData(Tag tag, ArrayList<String> strings) {
            super(tag, strings);
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
}
