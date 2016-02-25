package com.flipkart.batching.listener;

import android.os.Handler;

import com.flipkart.batching.Batch;
import com.flipkart.batching.Data;
import com.flipkart.batching.exception.DeserializeException;
import com.flipkart.batching.persistence.SerializationStrategy;
import com.squareup.tape.QueueFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by anirudh.r on 23/02/16.
 */
public abstract class TrimPersistedBatchReadyListener<E extends Data, T extends Batch<E>> extends PersistedBatchReadyListener<E, T> {

    private int trimSize, queueSize;
    private Collection<Data> removedCollection;
    private SerializationStrategy<E, T> serializationStrategy;

    public TrimPersistedBatchReadyListener(File file, SerializationStrategy<E, T> serializationStrategy, Handler handler, int queueSize, int trimSize) {
        super(file, serializationStrategy, handler);
        this.trimSize = trimSize;
        this.queueSize = queueSize;
        this.serializationStrategy = serializationStrategy;
        this.removedCollection = new ArrayList<>();
    }

    @Override
    protected void onInitialized(QueueFile queueFile) {
        super.onInitialized(queueFile);
        trimQueue(queueFile);
    }

    private void trimQueue(QueueFile queueFile) {
        if (queueFile.size() == queueSize) {
            for (int i = 0; i < trimSize; i++) {
                try {
                    removedCollection.add(serializationStrategy.deserializeData(queueFile.peek()));
                    queueFile.remove();
                } catch (DeserializeException | IOException e) {
                    e.printStackTrace();
                }
            }
            onTrimmed(queueFile.size(), queueFile.size() - trimSize, removedCollection);
        }
    }

    public abstract void onTrimmed(int oldSize, int newSize, Collection<Data> dataCollection);

}
