/*
 * Copyright 2016 Flipkart Internet Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flipkart.batching.strategy;

import android.content.Context;
import android.os.Handler;

import com.flipkart.Utils;
import com.flipkart.batching.Batch;
import com.flipkart.batching.BatchController;
import com.flipkart.batching.BatchingStrategy;
import com.flipkart.batching.BuildConfig;
import com.flipkart.batching.Data;
import com.flipkart.batching.OnBatchReadyListener;
import com.flipkart.batching.persistence.PersistenceStrategy;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Collection;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for {@link BaseBatchingStrategy}
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class BaseBatchingStrategyTest {

    /**
     * Test for {@link BaseBatchingStrategy#onDataPushed(Collection)}
     */
    @Test
    public void testOnDataPushed() {
        PersistenceStrategy<Data> persistenceStrategy = mock(PersistenceStrategy.class);

        BaseBatchingStrategy<Data, Batch<Data>> baseBatchingStrategy = new BaseBatchingStrategy<Data, Batch<Data>>(persistenceStrategy) {
            @Override
            public void flush(boolean forced) {

            }
        };

        ArrayList<Data> arrayList = Utils.fakeCollection(4);
        baseBatchingStrategy.onDataPushed(arrayList);
        baseBatchingStrategy.flush(false);
        when(persistenceStrategy.getData()).thenReturn(arrayList);
        when(persistenceStrategy.getDataSize()).thenReturn(arrayList.size());
        //verify that it gets called once
        verify(persistenceStrategy, times(1)).add(arrayList);
    }

    /**
     * Test for {@link BatchingStrategy#onInitialized(Context, OnBatchReadyListener, Handler)}
     */
    @Test
    public void testOnInitialized() {
        PersistenceStrategy<Data> persistenceStrategy = mock(PersistenceStrategy.class);
        Context context = RuntimeEnvironment.application;
        OnBatchReadyListener onBatchReadyListener = mock(OnBatchReadyListener.class);
        Handler handler = new Handler();

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

    /**
     * Test that {@link BaseBatchingStrategy#getPersistenceStrategy()} is not null
     */
    @Test
    public void testPersistenceNotNull() {
        PersistenceStrategy<Data> persistenceStrategy = mock(PersistenceStrategy.class);
        BatchController batchController = mock(BatchController.class);
        Context context = RuntimeEnvironment.application;
        OnBatchReadyListener onBatchReadyListener = mock(OnBatchReadyListener.class);
        Handler handler = new Handler();

        BaseBatchingStrategy<Data, Batch<Data>> baseBatchingStrategy = new BaseBatchingStrategy<Data, Batch<Data>>(persistenceStrategy) {
            @Override
            public void flush(boolean forced) {

            }
        };
        baseBatchingStrategy.onInitialized(context, onBatchReadyListener, handler);

        Assert.assertNotNull(baseBatchingStrategy.getPersistenceStrategy());
    }

    /**
     * Throw {@link IllegalArgumentException} when {@link BaseBatchingStrategy#getPersistenceStrategy()} is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void testPersistenceNull() {
        Context context = RuntimeEnvironment.application;
        OnBatchReadyListener onBatchReadyListener = mock(OnBatchReadyListener.class);
        Handler handler = new Handler();

        BaseBatchingStrategy<Data, Batch<Data>> baseBatchingStrategy = new BaseBatchingStrategy<Data, Batch<Data>>(null) {
            @Override
            public void flush(boolean forced) {

            }
        };
        baseBatchingStrategy.onInitialized(context, onBatchReadyListener, handler);

        Assert.assertNotNull(baseBatchingStrategy.getPersistenceStrategy());
    }
}
