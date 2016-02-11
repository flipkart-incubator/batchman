package com.flipkart.batching;

import android.content.Context;
import android.os.Handler;

import com.flipkart.data.Data;
import com.flipkart.exception.IllegalArgumentException;
import com.flipkart.exception.PersistenceNullException;
import com.flipkart.persistence.PersistenceStrategy;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SizeBatchingTest {

    @Mock
    private PersistenceStrategy persistenceStrategy;
    @Mock
    private Data eventData;
    @Mock
    private Context context;
    @Mock
    private BatchController controller;
    @Mock
    private OnBatchReadyListener onBatchReadyListener;
    @Mock
    private Handler handler;
    @Mock
    private IllegalArgumentException illegalArgumentException;
    @Mock
    private PersistenceNullException persistenceNullException;

    private SizeBatchingStrategy sizeBatchingStrategy;

    protected int BATCH_SIZE = 5;

    /**
     * Setting up the test environment.
     *
     * @throws PersistenceNullException
     * @throws IllegalArgumentException
     */
    @Before
    public void setUp() throws PersistenceNullException, IllegalArgumentException {
        MockitoAnnotations.initMocks(this);
        sizeBatchingStrategy = new SizeBatchingStrategy(BATCH_SIZE, persistenceStrategy);
        sizeBatchingStrategy.onInitialized(controller, context, onBatchReadyListener, handler);
    }

    /**
     * This test is to ensure the working of {@link SizeBatchingStrategy#onDataPushed(Collection)} )
     */
    @Test
    public void testOnDataPushed() {
        sizeBatchingStrategy.onDataPushed(Collections.singleton(eventData));
        verify(persistenceStrategy, times(1)).add(anyCollection());
    }

    /**
     * This test is to ensure the working of {@link SizeBatchingStrategy#flush(boolean)}
     * Whenever this method is invoked, {@link com.flipkart.persistence.PersistenceStrategy#removeData(Collection)} should be called
     */
    @Test
    public void testFlush() {
        sizeBatchingStrategy.flush(true);
        verify(persistenceStrategy, times(1)).removeData(anyCollection());
    }

    /**
     * This test is to check the {@link OnBatchReadyListener#onReady(Collection)} callback for various uses cases.
     * Flush is False for this test
     */
    @Test
    public void testOnReadyCallbackFlushFalse() {

        //pushed 1 data, onReady should NOT get called.
        reset(onBatchReadyListener);
        ArrayList<Data> data = fakeCollection(1);
        sizeBatchingStrategy.onDataPushed(data);
        when(persistenceStrategy.getData()).thenReturn(data);
        sizeBatchingStrategy.flush(false);
        verify(onBatchReadyListener, times(0)).onReady(data);

        //pushed 2 data, onReady should NOT get called.
        reset(onBatchReadyListener);
        data = fakeCollection(2);
        sizeBatchingStrategy.onDataPushed(data);
        when(persistenceStrategy.getData()).thenReturn(data);
        sizeBatchingStrategy.flush(false);
        verify(onBatchReadyListener, times(0)).onReady(data);

        //pushed 3 data, onReady should NOT get called.
        reset(onBatchReadyListener);
        data = fakeCollection(3);
        sizeBatchingStrategy.onDataPushed(data);
        when(persistenceStrategy.getData()).thenReturn(data);
        sizeBatchingStrategy.flush(false);
        verify(onBatchReadyListener, times(0)).onReady(data);

        //pushed 4 data, onReady should NOT get called.
        reset(onBatchReadyListener);
        data = fakeCollection(4);
        sizeBatchingStrategy.onDataPushed(data);
        when(persistenceStrategy.getData()).thenReturn(data);
        sizeBatchingStrategy.flush(false);
        verify(onBatchReadyListener, times(0)).onReady(data);

        //pushed 5 data, onReady should GET called.
        reset(onBatchReadyListener);
        data = fakeCollection(5);
        sizeBatchingStrategy.onDataPushed(data);
        when(persistenceStrategy.getData()).thenReturn(data);
        sizeBatchingStrategy.flush(false);
        verify(onBatchReadyListener, times(1)).onReady(data);
    }

    /**
     * This test is to check the {@link OnBatchReadyListener#onReady(Collection)} callback for various uses cases.
     * Flush is TRUE for this test. {@link OnBatchReadyListener#onReady(Collection)} should be called every time.
     */
    @Test
    public void testOnReadyCallbackFlushTrue() {

        //pushed 1 data, onReady should be called with flush true
        reset(onBatchReadyListener);
        ArrayList<Data> data = fakeCollection(1);
        sizeBatchingStrategy.onDataPushed(data);
        when(persistenceStrategy.getData()).thenReturn(data);
        sizeBatchingStrategy.flush(true);
        verify(onBatchReadyListener, times(1)).onReady(data);

        //pushed 2 data, onReady should be called with flush true
        reset(onBatchReadyListener);
        data = fakeCollection(2);
        sizeBatchingStrategy.onDataPushed(data);
        when(persistenceStrategy.getData()).thenReturn(data);
        sizeBatchingStrategy.flush(true);
        verify(onBatchReadyListener, times(1)).onReady(data);
    }

    /**
     * This test is to check the {@link OnBatchReadyListener#onReady(Collection)} callback.
     * This test ensures the integrity of the data.
     */
    @Test
    public void testOnReadyCallbackData() {

        reset(onBatchReadyListener);
        ArrayList<Data> data = fakeCollection(BATCH_SIZE - 1);
        sizeBatchingStrategy.onDataPushed(data);
        when(persistenceStrategy.getData()).thenReturn(data);
        sizeBatchingStrategy.flush(false);
        //verify that onReady DID NOT get called since we have pushed less than batch size events.
        verify(onBatchReadyListener, times(0)).onReady(data);
        List<Data> singleData = Collections.singletonList(eventData);
        data.addAll(singleData);

        reset(onBatchReadyListener);
        sizeBatchingStrategy.onDataPushed(singleData);
        when(persistenceStrategy.getData()).thenReturn(data);
        sizeBatchingStrategy.flush(false);
        //verify that onReady DID get called since we have pushed enough events equal to batch size
        verify(onBatchReadyListener, times(1)).onReady(eq(data));
        data.clear();
        singleData = Collections.singletonList(eventData);
        data.addAll(singleData);

        reset(onBatchReadyListener);
        sizeBatchingStrategy.onDataPushed(data);
        when(persistenceStrategy.getData()).thenReturn(data);
        sizeBatchingStrategy.flush(false);
        //verify that onReady DID NOT get called since we have only one event remaining
        verify(onBatchReadyListener, times(0)).onReady(eq(data));
    }

    /**
     * This test to check the {@link OnBatchReadyListener#onReady(Collection)} callback when the
     * data list passed is empty
     */
    @Test
    public void testOnReadyForEmptyData() {

        reset(onBatchReadyListener);
        ArrayList<Data> data = new ArrayList<>();
        sizeBatchingStrategy.onDataPushed(data);
        when(persistenceStrategy.getData()).thenReturn(data);
        sizeBatchingStrategy.flush(false);
        //verify that onReady is NOT called since the data list is empty.
        verify(onBatchReadyListener, times(0)).onReady(data);
    }

    /**
     * This test is to throw an exception whenever the batch size is less than or equal to 0.
     *
     * @throws IllegalArgumentException
     * @throws PersistenceNullException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIfBatchSizeIsZero() throws IllegalArgumentException, PersistenceNullException {
        sizeBatchingStrategy = new SizeBatchingStrategy(0, persistenceStrategy);
    }

    /**
     * This test is to throw an exception whenever the persistence strategy is null.
     *
     * @throws IllegalArgumentException
     * @throws PersistenceNullException
     */
    @Test(expected = PersistenceNullException.class)
    public void testIfPersistenceNull() throws IllegalArgumentException, PersistenceNullException {
        sizeBatchingStrategy = new SizeBatchingStrategy(5, null);
    }

    /**
     * Method to create fake array list of Data.
     *
     * @param size
     * @return
     */
    protected ArrayList<Data> fakeCollection(int size) {
        ArrayList<Data> datas = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            datas.add(eventData);
        }
        return datas;
    }
}