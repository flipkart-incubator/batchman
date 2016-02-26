package com.flipkart.batching.listener;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.flipkart.Utils;
import com.flipkart.batching.Batch;
import com.flipkart.batching.BatchManager;
import com.flipkart.batching.BuildConfig;
import com.flipkart.batching.Data;
import com.flipkart.batching.exception.SerializeException;
import com.flipkart.batching.persistence.GsonSerializationStrategy;
import com.flipkart.batching.persistence.SerializationStrategy;
import com.squareup.tape.QueueFile;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by anirudh.r on 25/02/16.
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class TrimPersistedBatchReadyTest {

    TrimPersistedBatchReadyListener<Data, Batch<Data>> trimPersistedBatchReadyListener;
    SerializationStrategy<Data, Batch<Data>> serializationStrategy;
    QueueFile queueFile;
    File file;

    @Test
    public void testTrimData() {
        file = new File("testfile");
        serializationStrategy = new GsonSerializationStrategy<>();
        serializationStrategy.build();
        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        Handler handler = new Handler(looper);

        trimPersistedBatchReadyListener = new TrimPersistedBatchReadyListener<Data, Batch<Data>>(file, serializationStrategy,
                handler, 10, 5) {
            @Override
            public void onTrimmed(int oldSize, int newSize, Collection<Data> dataCollection) {

            }

            @Override
            public void onPersistSuccess(Batch<Data> batch) {

            }

            @Override
            public void onPersistFailure(Batch<Data> batch, Exception e) {

            }
        };
    }

    @Test
    public void testOnTrimPersistSuccess() throws IOException, SerializeException {
        file = new File("testfile");
        queueFile = new QueueFile(file);
        serializationStrategy = new GsonSerializationStrategy<>();
        BatchManager.registerBuiltInTypes(serializationStrategy);
        serializationStrategy.build();
        final ArrayList<Data> datas = Utils.fakeAdsCollection(4);

        for (Data d : datas) {
            queueFile.add(serializationStrategy.serializeData(d));
        }

        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        Handler handler = new Handler(looper);

        trimPersistedBatchReadyListener = new TrimPersistedBatchReadyListener<Data, Batch<Data>>(file, serializationStrategy,
                handler, 10, 5) {
            @Override
            public void onTrimmed(int oldSize, int newSize, Collection<Data> dataCollection) {

            }

            @Override
            public void onPersistSuccess(Batch<Data> batch) {
                Assert.assertEquals(batch.getDataCollection(), datas);
            }

            @Override
            public void onPersistFailure(Batch<Data> batch, Exception e) {

            }
        };

        trimPersistedBatchReadyListener.onInitialized(queueFile);
    }

    @Test
    public void testOnTrimmedCalled() throws IOException, SerializeException {
        file = new File("testfile");
        queueFile = new QueueFile(file);
        serializationStrategy = new GsonSerializationStrategy<>();
        BatchManager.registerBuiltInTypes(serializationStrategy);
        serializationStrategy.build();
        final ArrayList<Data> datas = Utils.fakeAdsCollection(5);

        for (Data d : datas) {
            queueFile.add(serializationStrategy.serializeData(d));
        }

        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        ShadowLooper shadowLooper = Shadows.shadowOf(looper);
        Handler handler = new Handler(looper);

        trimPersistedBatchReadyListener = new TrimPersistedBatchReadyListener<Data, Batch<Data>>(file, serializationStrategy,
                handler, 5, 1) {
            @Override
            public void onTrimmed(int oldSize, int newSize, Collection<Data> dataCollection) {
                Assert.assertTrue(oldSize == 5);
                Assert.assertTrue(newSize == 4);
            }

            @Override
            public void onPersistSuccess(Batch<Data> batch) {
            }

            @Override
            public void onPersistFailure(Batch<Data> batch, Exception e) {

            }
        };

        trimPersistedBatchReadyListener.onInitialized(queueFile);
    }

}
