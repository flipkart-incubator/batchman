package com.flipkart.batching;

import android.content.Context;
import android.os.Handler;

import com.flipkart.Utils;
import com.flipkart.batching.persistence.PersistenceStrategy;
import com.flipkart.batching.strategy.BaseBatchingStrategy;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Collection;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by anirudh.r on 15/02/16.
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class BaseBatchingStrategyTest {

    @Mock
    PersistenceStrategy<Data> persistenceStrategy;
    @Mock
    BatchController batchController;
    @Mock
    Context context;
    @Mock
    OnBatchReadyListener<Data, Batch<Data>> onBatchReadyListener;
    @Mock
    Handler handler;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test {@link BaseBatchingStrategy#onDataPushed(Collection)}
     */
    @Test
    public void testOnDataPushed() {
        BaseBatchingStrategy<Data, Batch<Data>> baseBatchingStrategy = new BaseBatchingStrategy<Data, Batch<Data>>(persistenceStrategy) {
            @Override
            public void flush(boolean forced) {

            }
        };

        ArrayList<Data> arrayList = Utils.fakeCollection(4);
        baseBatchingStrategy.onDataPushed(arrayList);
        baseBatchingStrategy.flush(false);
        when(persistenceStrategy.getData()).thenReturn(arrayList);
        verify(persistenceStrategy, times(1)).add(arrayList);
    }

    /**
     * Test {@link BatchingStrategy#onInitialized(Context, OnBatchReadyListener, Handler)}
     */
    @Test
    public void testOnInitialized() {
        BaseBatchingStrategy<Data, Batch<Data>> baseBatchingStrategy = new BaseBatchingStrategy<Data, Batch<Data>>(persistenceStrategy) {
            @Override
            public void flush(boolean forced) {

            }
        };
        baseBatchingStrategy.onInitialized(context, onBatchReadyListener, handler);
        Assert.assertTrue(baseBatchingStrategy.isInitialized());

        Assert.assertNotNull(baseBatchingStrategy.getContext());
        Assert.assertNotNull(baseBatchingStrategy.getOnReadyListener());
    }
}
