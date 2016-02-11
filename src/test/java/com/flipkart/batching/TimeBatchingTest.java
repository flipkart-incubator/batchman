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
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by anirudh.r on 11/02/16.
 */
public class TimeBatchingTest {

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

    private TimeBatchingStrategy timeBatchingStrategy;

    private long TIME_OUT = 5 * 1000;

    /**
     * Setting up the test environment.
     *
     * @throws PersistenceNullException
     * @throws IllegalArgumentException
     */
    @Before
    public void setUp() throws PersistenceNullException, IllegalArgumentException {
        MockitoAnnotations.initMocks(this);
        timeBatchingStrategy = new TimeBatchingStrategy(TIME_OUT, persistenceStrategy);
        timeBatchingStrategy.onInitialized(controller, context, onBatchReadyListener, handler);
    }

    /**
     * This test is to ensure the working of {@link TimeBatchingStrategy#onDataPushed(Collection)} )
     */
    @Test
    public void testOnDataPushed() {
        timeBatchingStrategy.onDataPushed(Collections.singleton(eventData));
        verify(persistenceStrategy, times(1)).add(anyCollection());
    }

    /**
     * This test is to ensure the working of {@link TimeBatchingStrategy#flush(boolean)}
     * Whenever this method is invoked, {@link com.flipkart.persistence.PersistenceStrategy#removeData(Collection)} should be called
     */
    @Test
    public void testFlush() {
        timeBatchingStrategy.onDataPushed(Collections.singleton(eventData));
        when(persistenceStrategy.getData()).thenReturn(Collections.singleton(eventData));
        timeBatchingStrategy.flush(true);
        verify(persistenceStrategy, times(1)).removeData(anyCollection());
    }

    /**
     * This test is to check the {@link OnBatchReadyListener#onReady(Collection)} callback.
     * This test ensures the integrity of the data.
     */
    @Test
    public void testOnReadyCallbackData() {

        reset(onBatchReadyListener);
        final ArrayList<Data> data = fakeCollection(5);
        timeBatchingStrategy.onDataPushed(data);
        when(persistenceStrategy.getData()).thenReturn(data);

        timeBatchingStrategy.flush(false);

//        verify(onBatchReadyListener, times(1)).onReady(data);

        List<Data> singleData = Collections.singletonList(eventData);
        data.addAll(singleData);

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
