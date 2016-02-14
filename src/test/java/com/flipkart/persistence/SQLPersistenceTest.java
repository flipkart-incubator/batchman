package com.flipkart.persistence;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.flipkart.Utils;
import com.flipkart.batching.BuildConfig;
import com.flipkart.data.Data;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
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
    private ShadowLooper shadowLooper;
    private Handler handler;
    private Context context;


    /**
     * Test to verify that data is retained in InMemoryList after {@link PersistenceStrategy#add(Collection)} is called.
     * It also verifies the {@link SQLPersistenceStrategy#syncData()}
     */
    @Test
    public void testIfDataIsPersisted() {

        initializeSQLPersistence();
        ArrayList<Data> data = Utils.fakeCollection(5);
        persistenceStrategy.add(data);
        shadowLooper.runToEndOfTasks();
        persistenceStrategy = new SQLPersistenceStrategy(new GsonSerializationStrategy(), context, handler);
        shadowLooper.runToEndOfTasks();
        //verify that the data that was added, has been persisted and it correct form
        Assert.assertEquals(data, persistenceStrategy.getData());
    }

    /**
     * Test to verify that the data has been deleted from the db
     * Also verifies that {@link SQLPersistenceStrategy#syncData()} should not contain any data
     */
    @Test
    public void testIfDataIsRemoved() {
        initializeSQLPersistence();
        ArrayList<Data> dataArrayList = Utils.fakeCollection(5);
        persistenceStrategy.add(dataArrayList);
        shadowLooper.runToEndOfTasks();

        //verify that the data that was added, has been persisted and it correct form
        persistenceStrategy = new SQLPersistenceStrategy(new GsonSerializationStrategy(), context, handler);
        shadowLooper.runToEndOfTasks();
        Assert.assertEquals(dataArrayList, persistenceStrategy.getData());

        persistenceStrategy.removeData(dataArrayList);
        shadowLooper.runToEndOfTasks();
        //verify that the data has been deleted from the persistence layer
        Assert.assertEquals(persistenceStrategy.getData().size(), 0);
    }

    /**
     * Initialize the SQLPersistence
     */
    private void initializeSQLPersistence() {
        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        shadowLooper = Shadows.shadowOf(looper);
        handler = new Handler(looper);
        context = RuntimeEnvironment.application;
        persistenceStrategy = new SQLPersistenceStrategy(new GsonSerializationStrategy(), context, handler);
    }
}
