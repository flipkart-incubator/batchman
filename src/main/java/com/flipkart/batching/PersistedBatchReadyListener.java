package com.flipkart.batching;

import android.os.Handler;
import android.util.Log;

import com.flipkart.data.Batch;
import com.flipkart.data.Data;
import com.flipkart.exception.DeserializeException;
import com.flipkart.persistence.SerializationStrategy;
import com.squareup.tape.QueueFile;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by kushal.sharma on 13/02/16.
 */

public abstract class PersistedBatchReadyListener implements OnBatchReadyListener {
    private final SerializationStrategy serializationStrategy;
    private final File file;
    private final Handler handler;
    private QueueFile queueFile;
    private boolean initialized;
    private boolean isWaitingToFinish;

    public PersistedBatchReadyListener(File file, SerializationStrategy serializationStrategy, Handler handler) {
        this.file = file;
        this.serializationStrategy = serializationStrategy;
        this.handler = handler;
        handler.post(new Runnable() {
            @Override
            public void run() {
                //checkPending();
            }
        });
    }

    public abstract void onPersistSuccess(BatchInfo batchInfo, Collection<Data> batchedData);

    public abstract void onPersistFailure(Collection<Data> batchedData, Exception e);

    @Override
    public void onReady(BatchingStrategy causingStrategy, final BatchInfo batchInfo, final Collection<Data> dataCollection) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                initializeIfRequired();
                try {
                    queueFile.add(serializationStrategy.serializeBatch(new Batch(batchInfo, dataCollection)));
                    callPersistSuccess(batchInfo, dataCollection);
                } catch (Exception e) {
                    onPersistFailure(dataCollection, e);
                }
            }
        });
    }

    private void callPersistSuccess(BatchInfo batchInfo, Collection<Data> dataCollection) {
        if (!isWaitingToFinish) {
            isWaitingToFinish = true;
            onPersistSuccess(batchInfo, dataCollection);
        }
    }

    private void initializeIfRequired() {
        if (!initialized) {
            initialized = true;
            try {
                this.queueFile = new QueueFile(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void finish() {
        if (!queueFile.isEmpty()) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        queueFile.remove();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    isWaitingToFinish = false;
                    checkPending();
                }
            });
        }
    }

    private void checkPending() {
        initializeIfRequired();
        Log.e("PersistReadyListener", String.valueOf(queueFile.size()));
        if (!queueFile.isEmpty()) {

            try {
                byte[] eldest = queueFile.peek();
                if (eldest != null) {
                    Log.e("PersistReadyListener", "eldest is not null");
                    Batch batch = serializationStrategy.deserializeBatch(eldest);
                    callPersistSuccess(batch.getBatchInfo(), batch.getDataCollection());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (DeserializeException e) {
                e.printStackTrace();
            }
        }
    }
}
