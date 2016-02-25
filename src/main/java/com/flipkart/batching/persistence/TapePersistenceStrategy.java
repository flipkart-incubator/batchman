package com.flipkart.batching.persistence;

import com.flipkart.batching.Batch;
import com.flipkart.batching.Data;
import com.flipkart.batching.exception.DeserializeException;
import com.flipkart.batching.exception.SerializeException;
import com.squareup.tape.QueueFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by kushal.sharma on 23/02/16.
 * Simple class for Tape Persistence Strategy that extends In Memory Persistence Strategy
 */

public class TapePersistenceStrategy<E extends Data> extends InMemoryPersistenceStrategy<E> {

    private File file;
    private QueueFile queueFile;
    private SerializationStrategy<E, ? extends Batch> serializationStrategy;

    public TapePersistenceStrategy(File file, SerializationStrategy<E, ? extends Batch> serializationStrategy) {
        this.file = file;
        this.serializationStrategy = serializationStrategy;
    }

    @Override
    public void add(Collection<E> dataCollection) {
        super.add(dataCollection);
        for (E data : dataCollection) {
            try {
                queueFile.add(serializationStrategy.serializeData(data));
            } catch (IOException | SerializeException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void removeData(Collection<E> dataCollection) {
        super.removeData(dataCollection);
        for (int i = 0; i < dataCollection.size(); i++) {
            try {
                if (dataCollection.contains(serializationStrategy.deserializeData(queueFile.peek()))) {
                    try {
                        queueFile.remove();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (DeserializeException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onInitialized() {
        if (!isInitialized()) {
            try {
                this.queueFile = new QueueFile(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            syncData();
        }
        super.onInitialized();
    }

    private void syncData() {
        super.add(getAllDataFromTapeQueue());
    }

    private Collection<E> getAllDataFromTapeQueue() {
        ArrayList<E> dataList = new ArrayList<>();
        int size = queueFile.size();
        for (int i = 0; i < size; i++) {
            try {
                E data = serializationStrategy.deserializeData(queueFile.peek());
                dataList.add(data);
                queueFile.remove();
                queueFile.add(serializationStrategy.serializeData(data));
            } catch (DeserializeException e) {
                e.getRealException().printStackTrace();
            } catch (IOException | SerializeException e) {
                e.printStackTrace();
            }
        }
        return dataList;
    }
}
