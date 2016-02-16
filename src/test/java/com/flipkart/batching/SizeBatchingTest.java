//package com.flipkart.batching;
//
//import android.content.Context;
//import android.os.Handler;
//
//import com.flipkart.Utils;
//import com.flipkart.data.Data;
//import com.flipkart.persistence.PersistenceStrategy;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.List;
//
//import static org.mockito.Matchers.eq;
//import static org.mockito.Mockito.reset;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//public class SizeBatchingTest {
//
//    @Mock
//    private PersistenceStrategy persistenceStrategy;
//    @Mock
//    private Data eventData;
//    @Mock
//    private Context context;
//    @Mock
//    private BatchController controller;
//    @Mock
//    private OnBatchReadyListener onBatchReadyListener;
//    @Mock
//    private Handler handler;
//
//    private int BATCH_SIZE = 5;
//
//    /**
//     * Setting up the test environment.
//     */
//    @Before
//    public void setUp() {
//        MockitoAnnotations.initMocks(this);
//    }
//
//    /**
//     * This test is to ensure the working of {@link SizeBatchingStrategy#onDataPushed(Collection)} )
//     */
//    @Test
//    public void testOnDataPushed() {
//        SizeBatchingStrategy sizeBatchingStrategy = new SizeBatchingStrategy(BATCH_SIZE, persistenceStrategy);
//        sizeBatchingStrategy.onInitialized(controller, context, onBatchReadyListener, handler);
//        ArrayList<Data> fakeCollection = Utils.fakeCollection(1);
//        sizeBatchingStrategy.onDataPushed(fakeCollection);
//        verify(persistenceStrategy, times(1)).add(eq(fakeCollection));
//    }
//
//    /**
//     * This test is to ensure the working of {@link SizeBatchingStrategy#flush(boolean)}
//     * Whenever this method is invoked, {@link com.flipkart.persistence.PersistenceStrategy#removeData(Collection)} should be called
//     */
//    @Test
//    public void testFlush() {
//        SizeBatchingStrategy sizeBatchingStrategy = new SizeBatchingStrategy(BATCH_SIZE, persistenceStrategy);
//        sizeBatchingStrategy.onInitialized(controller, context, onBatchReadyListener, handler);
//        ArrayList<Data> fakeCollection = Utils.fakeCollection(1);
//        sizeBatchingStrategy.onDataPushed(fakeCollection);
//        when(persistenceStrategy.getData()).thenReturn(fakeCollection);
//        sizeBatchingStrategy.flush(true);
//        verify(persistenceStrategy, times(1)).removeData(eq(fakeCollection));
//    }
//
//    /**
//     * This test is to check the {@link OnBatchReadyListener#onReady(BatchingStrategy, Collection)} callback for various uses cases.
//     * Flush is False for this test
//     */
//    @Test
//    public void testOnReadyCallbackFlushFalse() {
//        SizeBatchingStrategy sizeBatchingStrategy = new SizeBatchingStrategy(BATCH_SIZE, persistenceStrategy);
//        sizeBatchingStrategy.onInitialized(controller, context, onBatchReadyListener, handler);
//
//        //pushed 1 data, onReady should NOT get called.
//        ArrayList<Data> data = Utils.fakeCollection(1);
//        sizeBatchingStrategy.onDataPushed(data);
//        when(persistenceStrategy.getData()).thenReturn(data);
//        sizeBatchingStrategy.flush(false);
//        verify(onBatchReadyListener, times(0)).onReady(data);
//
//        //pushed 2 data, onReady should NOT get called.
//        sizeBatchingStrategy = new SizeBatchingStrategy(BATCH_SIZE, persistenceStrategy);
//        sizeBatchingStrategy.onInitialized(controller, context, onBatchReadyListener, handler);
//        data = Utils.fakeCollection(2);
//        sizeBatchingStrategy.onDataPushed(data);
//        when(persistenceStrategy.getData()).thenReturn(data);
//        sizeBatchingStrategy.flush(false);
//        verify(onBatchReadyListener, times(0)).onReady(data);
//
//        //pushed 3 data, onReady should NOT get called.
//        sizeBatchingStrategy = new SizeBatchingStrategy(BATCH_SIZE, persistenceStrategy);
//        sizeBatchingStrategy.onInitialized(controller, context, onBatchReadyListener, handler);
//        data = Utils.fakeCollection(3);
//        sizeBatchingStrategy.onDataPushed(data);
//        when(persistenceStrategy.getData()).thenReturn(data);
//        sizeBatchingStrategy.flush(false);
//        verify(onBatchReadyListener, times(0)).onReady(data);
//
//        //pushed 4 data, onReady should NOT get called.
//        sizeBatchingStrategy = new SizeBatchingStrategy(BATCH_SIZE, persistenceStrategy);
//        sizeBatchingStrategy.onInitialized(controller, context, onBatchReadyListener, handler);
//        data = Utils.fakeCollection(4);
//        sizeBatchingStrategy.onDataPushed(data);
//        when(persistenceStrategy.getData()).thenReturn(data);
//        sizeBatchingStrategy.flush(false);
//        verify(onBatchReadyListener, times(0)).onReady(data);
//
//        //pushed 5 data, onReady should GET called.
//        sizeBatchingStrategy = new SizeBatchingStrategy(BATCH_SIZE, persistenceStrategy);
//        sizeBatchingStrategy.onInitialized(controller, context, onBatchReadyListener, handler);
//        data = Utils.fakeCollection(5);
//        sizeBatchingStrategy.onDataPushed(data);
//        when(persistenceStrategy.getData()).thenReturn(data);
//        sizeBatchingStrategy.flush(false);
//        verify(onBatchReadyListener, times(1)).onReady(data);
//    }
//
//    /**
//     * This test is to check the {@link OnBatchReadyListener#onReady(BatchingStrategy, Collection)} callback for various uses cases.
//     * Flush is TRUE for this test. {@link OnBatchReadyListener#onReady(BatchingStrategy, Collection)} should be called every time.
//     */
//    @Test
//    public void testOnReadyCallbackFlushTrue() {
//
//        SizeBatchingStrategy sizeBatchingStrategy = new SizeBatchingStrategy(BATCH_SIZE, persistenceStrategy);
//        sizeBatchingStrategy.onInitialized(controller, context, onBatchReadyListener, handler);
//        //pushed 1 data, onReady should be called with flush true
//        ArrayList<Data> data = Utils.fakeCollection(1);
//        sizeBatchingStrategy.onDataPushed(data);
//        when(persistenceStrategy.getData()).thenReturn(data);
//        sizeBatchingStrategy.flush(true);
//        verify(onBatchReadyListener, times(1)).onReady(eq(data));
//
//        //pushed 2 data, onReady should be called with flush true
//        sizeBatchingStrategy = new SizeBatchingStrategy(BATCH_SIZE, persistenceStrategy);
//        sizeBatchingStrategy.onInitialized(controller, context, onBatchReadyListener, handler);
//        data = Utils.fakeCollection(2);
//        sizeBatchingStrategy.onDataPushed(data);
//        when(persistenceStrategy.getData()).thenReturn(data);
//        sizeBatchingStrategy.flush(true);
//        verify(onBatchReadyListener, times(1)).onReady(eq(data));
//    }
//
//    /**
//     * This test is to check the {@link OnBatchReadyListener#onReady(BatchingStrategy, Collection)} callback.
//     * This test ensures the integrity of the data.
//     */
//    @Test
//    public void testOnReadyCallbackData() {
//        SizeBatchingStrategy sizeBatchingStrategy = new SizeBatchingStrategy(BATCH_SIZE, persistenceStrategy);
//        sizeBatchingStrategy.onInitialized(controller, context, onBatchReadyListener, handler);
//
//        ArrayList<Data> data = Utils.fakeCollection(BATCH_SIZE - 1);
//        sizeBatchingStrategy.onDataPushed(data);
//        when(persistenceStrategy.getData()).thenReturn(data);
//        sizeBatchingStrategy.flush(false);
//        //verify that onReady DID NOT get called since we have pushed less than batch size events.
//        verify(onBatchReadyListener, times(0)).onReady(eq(data));
//        List<Data> singleData = Collections.singletonList(eventData);
//        data.addAll(singleData);
//
//        reset(onBatchReadyListener);
//        sizeBatchingStrategy.onDataPushed(singleData);
//        when(persistenceStrategy.getData()).thenReturn(data);
//        sizeBatchingStrategy.flush(false);
//        //verify that onReady DID get called since we have pushed enough events equal to batch size
//        verify(onBatchReadyListener, times(1)).onReady(eq(data));
//        data.clear();
//        singleData = Collections.singletonList(eventData);
//        data.addAll(singleData);
//
//        reset(onBatchReadyListener);
//        sizeBatchingStrategy.onDataPushed(data);
//        when(persistenceStrategy.getData()).thenReturn(data);
//        sizeBatchingStrategy.flush(false);
//        //verify that onReady DID NOT get called since we have only one event remaining
//        verify(onBatchReadyListener, times(0)).onReady(eq(data));
//    }
//
//    /**
//     * This test to check the {@link OnBatchReadyListener#onReady(BatchingStrategy, Collection)} callback when the
//     * data list passed is empty
//     */
//    @Test
//    public void testOnReadyForEmptyData() {
//        SizeBatchingStrategy sizeBatchingStrategy = new SizeBatchingStrategy(BATCH_SIZE, persistenceStrategy);
//        sizeBatchingStrategy.onInitialized(controller, context, onBatchReadyListener, handler);
//        ArrayList<Data> data = new ArrayList<>();
//        sizeBatchingStrategy.onDataPushed(data);
//        when(persistenceStrategy.getData()).thenReturn(data);
//        sizeBatchingStrategy.flush(false);
//        //verify that onReady is NOT called since the data list is empty.
//        verify(onBatchReadyListener, times(0)).onReady(data);
//    }
//
//    /**
//     * This test is to check if it throws an exception whenever the batch size is 0.
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testIfBatchSizeIsZero() {
//        new SizeBatchingStrategy(0, persistenceStrategy);
//    }
//
//    /**
//     * This test is to check if it throws an exception whenever the batch size is less than 0.
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testIfBatchSizeNegative() {
//        new SizeBatchingStrategy(-4, persistenceStrategy);
//    }
//
//    /**
//     * This test is to throw an exception whenever the persistence strategy is null.
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testIfPersistenceNull() {
//        new SizeBatchingStrategy(5, null);
//    }
//}