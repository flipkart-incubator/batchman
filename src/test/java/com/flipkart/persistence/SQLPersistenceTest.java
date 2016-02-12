package com.flipkart.persistence;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.flipkart.batching.BuildConfig;
import com.flipkart.data.Data;
import com.flipkart.data.EventData;
import com.flipkart.data.Tag;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by anirudh.r on 11/02/16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class SQLPersistenceTest {

    private PersistenceStrategy persistenceStrategy;

    @Mock
    private Data eventData;

    @Mock
    private DatabaseHelper databaseHelper;
    private ShadowLooper shadowLooper;

    @Before
    public void setUp() {
        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        shadowLooper = Shadows.shadowOf(looper);

    }

    /**
     * Test to verify that data is retained in InMemoryList after {@link PersistenceStrategy#add(Collection)} is called.
     */
    @Test
    public void testIfDataInMemory() {
        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        shadowLooper = Shadows.shadowOf(looper);
        Handler handler = new Handler(looper);
        Context context = RuntimeEnvironment.application;
        persistenceStrategy = new SQLPersistenceStrategy(new GsonSerializationStrategy(), context, handler);

        Data eventData = new EventData(new Tag("1"), "e1");
        Data eventData1 = new EventData(new Tag("2"), "e2");
        Data eventData2 = new EventData(new Tag("3"), "e3");
        Data eventData3 = new EventData(new Tag("4"), "e4");
        Data eventData4 = new EventData(new Tag("5"), "e5");

        ArrayList<Data> data = new ArrayList<>();
        data.add(eventData);
        data.add(eventData1);
        data.add(eventData2);
        data.add(eventData3);
        data.add(eventData4);
        persistenceStrategy.add(data);
        shadowLooper.runToEndOfTasks();
        persistenceStrategy = new SQLPersistenceStrategy(new GsonSerializationStrategy(), context, handler);
        shadowLooper.runToEndOfTasks();
        Assert.assertEquals(data.size(),persistenceStrategy.getData().size());
    }
}
