package com.flipkart.batching;

import android.content.Context;
import android.os.Handler;

import com.flipkart.data.Data;
import com.flipkart.persistence.SerializationStrategy;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * Created by anirudh.r on 12/02/16.
 */
public class BatchManagerTest {

    BatchController batchController;

    @Mock
    SerializationStrategy serializationStrategy;
    @Mock
    SizeBatchingStrategy sizeBatchingStrategy;
    @Mock
    Handler handler;
    @Mock
    OnBatchReadyListener onBatchReadyListener;
    @Mock
    Context context;
    @Mock
    Data eventData;

    @Before
    public void setUp() {

        batchController = new BatchManager.Builder()
                .setSerializationStrategy(serializationStrategy)
                .setBatchingStrategy(sizeBatchingStrategy)
                .setHandler(handler)
                .setOnBatchReadyListener(onBatchReadyListener)
                .build(context);
    }

    @Test
    public void testAddtoBatch() {
        batchController.addToBatch(eventData);
    }
}
