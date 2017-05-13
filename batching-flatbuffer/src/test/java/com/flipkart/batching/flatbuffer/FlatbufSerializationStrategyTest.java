package com.flipkart.batching.flatbuffer;


import com.flipkart.batching.core.Data;
import com.flipkart.batching.core.batch.SizeBatch;
import com.flipkart.batching.core.data.EventData;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class FlatbufSerializationStrategyTest {

    /**
     * Method to create fake array list of Data.
     *
     * @param size
     * @return dataList
     */
    public static ArrayList<Data> fakeCollection(int size) {
        ArrayList<Data> dataList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Data eventData = new EventData();
            eventData.setEventId(System.currentTimeMillis() + System.nanoTime() + i);
            dataList.add(eventData);
        }
        return dataList;
    }

    @Test
    public void testBatchSerialization() throws Exception {
        FlatBufSerializationStrategy flatBufSerializationStrategy = new FlatBufSerializationStrategy();

        SizeBatch batch = new SizeBatch<>(fakeCollection(3), 3);
        byte[] bytes = flatBufSerializationStrategy.serializeBatch(batch);

        com.flipkart.batching.core.Batch deserializeBatch = flatBufSerializationStrategy.deserializeBatch(bytes);

        Assert.assertEquals(batch.getDataCollection().size(), deserializeBatch.getDataCollection().size());
        Assert.assertEquals(batch.getMaxBatchSize(), ((SizeBatch) deserializeBatch).getMaxBatchSize());
    }

    @Test
    public void testDataSerialization() throws Exception {
        FlatBufSerializationStrategy flatBufSerializationStrategy = new FlatBufSerializationStrategy();

        Data data = new Data() {
            @Override
            public long getEventId() {
                return 10;
            }
        };

        byte[] bytes = flatBufSerializationStrategy.serializeData(data);

        com.flipkart.batching.core.Data deserializeData = flatBufSerializationStrategy.deserializeData(bytes);

        Assert.assertEquals(10, deserializeData.getEventId());
    }
}