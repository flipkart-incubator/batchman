package com.flipkart.batching.flatbuffer;


import com.google.flatbuffers.FlatBufferBuilder;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.nio.ByteBuffer;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class FlatbufSerializationStrategy {

    @Test
    public void testBatchSerialization() throws Exception {
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder();

        Batch.startBatch(flatBufferBuilder);
        Batch.addMaxBatchSize(flatBufferBuilder, 10);
        Batch.addMaxTimeout(flatBufferBuilder, 10);
        int i = Batch.endBatch(flatBufferBuilder);
        flatBufferBuilder.finish(i);

        byte[] array = flatBufferBuilder.sizedByteArray();

        Assert.assertTrue(array.length != 0);

        ByteBuffer wrap = ByteBuffer.wrap(array);
        Batch b = Batch.getRootAsBatch(wrap);

        Assert.assertEquals(b.maxBatchSize(), 10);
        Assert.assertEquals(b.maxTimeout(), 10);
    }
}