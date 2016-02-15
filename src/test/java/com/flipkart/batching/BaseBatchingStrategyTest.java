package com.flipkart.batching;

import android.content.Context;
import android.os.Handler;

import com.flipkart.Utils;
import com.flipkart.data.Data;
import com.flipkart.persistence.PersistenceStrategy;

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
    PersistenceStrategy persistenceStrategy;
    @Mock
    BatchController batchController;
    @Mock
    Context context;
    @Mock
    OnBatchReadyListener onBatchReadyListener;
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
        BaseBatchingStrategy baseBatchingStrategy = new BaseBatchingStrategy(persistenceStrategy) {
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
     * Test {@link BaseBatchingStrategy#onInitialized(BatchController, Context, OnBatchReadyListener, Handler)}
     */
    @Test
    public void testOnInitialized() {
        BaseBatchingStrategy baseBatchingStrategy = new BaseBatchingStrategy(persistenceStrategy) {
            @Override
            public void flush(boolean forced) {

            }
        };
        baseBatchingStrategy.onInitialized(batchController, context, onBatchReadyListener, handler);
        Assert.assertTrue(baseBatchingStrategy.isInitialized());

        Assert.assertNotNull(baseBatchingStrategy.getBatchController());
        Assert.assertNotNull(baseBatchingStrategy.getContext());
        Assert.assertNotNull(baseBatchingStrategy.getOnReadyListener());
    }
}
