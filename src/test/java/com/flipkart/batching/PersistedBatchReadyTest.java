package com.flipkart.batching;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.flipkart.Utils;
import com.flipkart.data.Data;
import com.flipkart.exception.SerializeException;
import com.flipkart.persistence.ByteArraySerializationStrategy;
import com.flipkart.persistence.InMemoryPersistenceStrategy;
import com.flipkart.persistence.PersistenceStrategy;
import com.flipkart.persistence.SerializationStrategy;
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
import java.util.Collection;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

/**
 * Created by anirudh.r on 19/02/16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class PersistedBatchReadyTest {

    private PersistedBatchReadyListener persistedBatchReadyListener;
    private SerializationStrategy serializationStrategy;
    private ShadowLooper shadowLooper;
    private BatchingStrategy strategy;
    private PersistenceStrategy persistenceStrategy;
    private SizeBatchingStrategy.SizeBatchInfo sizeBatchInfo;
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
            public void onPersistSuccess(BatchInfo batchInfo, Collection<Data> batchedData) {
            }

            @Override
            public void onPersistFailure(Collection<Data> batchedData, Exception e) {
            }
        };
        ArrayList<Data> arrayList = Utils.fakeCollection(4);
        persistedBatchReadyListener.onReady(strategy, sizeBatchInfo, arrayList);
        shadowLooper.runToEndOfTasks();
        //should be initialized first time
        Assert.assertTrue(persistedBatchReadyListener.isInitialized());
    }

    /**
     * Test if {@link PersistedBatchReadyListener#onPersistSuccess(BatchInfo, Collection)}
     * is called with exact parameters.
     */
    @Test
    public void testIfPersistSuccessCalled() {
        init();
        final ArrayList<Data> arrayList = Utils.fakeCollection(4);

        persistedBatchReadyListener = new PersistedBatchReadyListener(new File("test"), serializationStrategy, handler) {
            @Override
            public void onPersistSuccess(BatchInfo batchInfo, Collection<Data> batchedData) {
                Assert.assertEquals(batchInfo, sizeBatchInfo);
                Assert.assertEquals(batchedData, arrayList);
            }

            @Override
            public void onPersistFailure(Collection<Data> batchedData, Exception e) {

            }
        };

        persistedBatchReadyListener.onReady(strategy, sizeBatchInfo, arrayList);
        shadowLooper.runToEndOfTasks();
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
            public void onPersistSuccess(BatchInfo batchInfo, Collection<Data> batchedData) {
            }

            @Override
            public void onPersistFailure(Collection<Data> batchedData, Exception e) {
            }
        };
        ArrayList<Data> arrayList = Utils.fakeCollection(4);

        when(queueFile.peek()).thenReturn(serializationStrategy.serializeCollection(arrayList));

        persistedBatchReadyListener.onReady(strategy, sizeBatchInfo, arrayList);
        shadowLooper.runToEndOfTasks();

        persistedBatchReadyListener.finish();
        shadowLooper.runToEndOfTasks();

        Assert.assertNotNull(queueFile.peek());

        doThrow(new IOException()).when(queueFile).remove();
        persistedBatchReadyListener.finish();
        shadowLooper.runToEndOfTasks();
    }

    @Test
    public void testIfPersistFailureCalled() throws SerializeException, IOException {
        init();
        final ArrayList<Data> arrayList = Utils.fakeCollection(4);
        persistedBatchReadyListener = new PersistedBatchReadyListener(new File("test"), serializationStrategy, handler) {
            @Override
            public void onPersistSuccess(BatchInfo batchInfo, Collection<Data> batchedData) {
            }

            @Override
            public void onPersistFailure(Collection<Data> batchedData, Exception e) {
                Assert.assertEquals(arrayList, batchedData);
            }
        };
        byte[] data = serializationStrategy.serializeCollection(arrayList);

        doThrow(new IOException()).when(queueFile).add(data);
        persistedBatchReadyListener.onReady(strategy, sizeBatchInfo, arrayList);
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
        sizeBatchInfo = new SizeBatchingStrategy.SizeBatchInfo(5);
        serializationStrategy = new ByteArraySerializationStrategy();
    }
}
