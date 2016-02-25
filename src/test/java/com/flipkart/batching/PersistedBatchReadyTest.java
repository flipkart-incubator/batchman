package com.flipkart.batching;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.flipkart.Utils;
import com.flipkart.batching.exception.SerializeException;
import com.flipkart.batching.listener.PersistedBatchReadyListener;
import com.flipkart.batching.persistence.ByteArraySerializationStrategy;
import com.flipkart.batching.persistence.InMemoryPersistenceStrategy;
import com.flipkart.batching.persistence.PersistenceStrategy;
import com.flipkart.batching.persistence.SerializationStrategy;
import com.flipkart.batching.strategy.SizeBatchingStrategy;
import com.squareup.tape.QueueFile;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

/**
 * Created by anirudh.r on 19/02/16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class PersistedBatchReadyTest {

    private PersistedBatchReadyListener persistedBatchReadyListener;
    private SerializationStrategy<Data, Batch<Data>> serializationStrategy;
    private ShadowLooper shadowLooper;
    private BatchingStrategy<Data, Batch<Data>> strategy;
    private PersistenceStrategy<Data> persistenceStrategy;
    private SizeBatchingStrategy.SizeBatch<Data> sizeBatchInfo;
    private Handler handler;
    @Mock
    private QueueFile queueFile;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

    }

    /**
     * Test if onReady initializes the file
     */
    @Test
    public void testIfInitializedCalled() {
        init();
        persistedBatchReadyListener = new PersistedBatchReadyListener(new File("test"), serializationStrategy, handler) {
            @Override
            public void onPersistSuccess(Batch batch) {

            }

            @Override
            public void onPersistFailure(Batch batch, Exception e) {

            }
        };

        ArrayList<Data> arrayList = Utils.fakeCollection(4);
        persistedBatchReadyListener.onReady(strategy, sizeBatchInfo);
        shadowLooper.runToEndOfTasks();
        //should be initialized first time
        Assert.assertTrue(persistedBatchReadyListener.isInitialized());
    }

    @Test
    public void testIfPersistSuccessCalled() {
        init();
        final ArrayList<Data> arrayList = Utils.fakeCollection(4);

        persistedBatchReadyListener = new PersistedBatchReadyListener(new File("test"), serializationStrategy, handler) {
            @Override
            public void onPersistSuccess(Batch batch) {
                Assert.assertEquals(batch, sizeBatchInfo);
                Assert.assertEquals(batch.getDataCollection(), arrayList);
            }

            @Override
            public void onPersistFailure(Batch batch, Exception e) {

            }
        };

        persistedBatchReadyListener.onReady(strategy, sizeBatchInfo);
    }

    /**
     * Test if Finish method gets called
     *
     * @throws IOException
     * @throws SerializeException
     */
    @Test
    public void testFinishCalled() throws IOException, SerializeException {
        init();
        persistedBatchReadyListener = new PersistedBatchReadyListener(new File("test"), serializationStrategy, handler) {
            @Override
            public void onPersistSuccess(Batch batch) {

            }

            @Override
            public void onPersistFailure(Batch batch, Exception e) {

            }
        };

        ArrayList<Data> arrayList = Utils.fakeCollection(4);

        when(queueFile.peek()).thenReturn(serializationStrategy.serializeCollection(arrayList));

        persistedBatchReadyListener.onReady(strategy, sizeBatchInfo);
        shadowLooper.runToEndOfTasks();

        persistedBatchReadyListener.finish();

        Assert.assertNotNull(queueFile.peek());

        doThrow(new IOException()).when(queueFile).remove();
        persistedBatchReadyListener.finish();
    }

    @Test
    public void testIfPersistFailureCalled() throws SerializeException, IOException {
        init();
        final ArrayList<Data> arrayList = Utils.fakeCollection(4);
        persistedBatchReadyListener = new PersistedBatchReadyListener(new File("test"), serializationStrategy, handler) {
            @Override
            public void onPersistSuccess(Batch batch) {

            }

            @Override
            public void onPersistFailure(Batch batch, Exception e) {
                Assert.assertEquals(arrayList, batch.getDataCollection());
            }
        };

        byte[] data = serializationStrategy.serializeCollection(arrayList);

        doThrow(new IOException()).when(queueFile).add(data);
        persistedBatchReadyListener.onReady(strategy, sizeBatchInfo);
        shadowLooper.runToEndOfTasks();

    }

    public void init() {
        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        shadowLooper = Shadows.shadowOf(looper);
        handler = new Handler(looper);
        persistenceStrategy = new InMemoryPersistenceStrategy();
        strategy = new SizeBatchingStrategy(5, persistenceStrategy);
        sizeBatchInfo = new SizeBatchingStrategy.SizeBatch<>(Utils.fakeCollection(5), 5);
        serializationStrategy = new ByteArraySerializationStrategy();
    }
}
