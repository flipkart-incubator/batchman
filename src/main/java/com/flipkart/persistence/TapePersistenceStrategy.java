package com.flipkart.persistence;

import com.flipkart.data.Data;
import com.flipkart.exception.DeserializeException;
import com.flipkart.exception.SerializeException;
import com.squareup.tape.QueueFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by kushal.sharma on 23/02/16.
 * Simple class for Tape Persistence Strategy that extends In Memory Persistence Strategy
 */

public class TapePersistenceStrategy extends InMemoryPersistenceStrategy {

    private File file;
    private QueueFile queueFile;
    private SerializationStrategy serializationStrategy;

    public TapePersistenceStrategy(File file, SerializationStrategy serializationStrategy) {
        this.file = file;
        this.serializationStrategy = serializationStrategy;
    }

    @Override
    public void add(Collection<Data> dataCollection) {
        super.add(dataCollection);
        for (Data data : dataCollection) {
            try {
                queueFile.add(serializationStrategy.serializeData(data));
            } catch (IOException | SerializeException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void removeData(Collection<Data> dataCollection) {
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

    private Collection<Data> getAllDataFromTapeQueue() {
        ArrayList<Data> dataList = new ArrayList<>();
        int size = queueFile.size();
        for (int i = 0; i < size; i++) {
            try {
                Data data = serializationStrategy.deserializeData(queueFile.peek());
                dataList.add(data);
                queueFile.remove();
                queueFile.add(serializationStrategy.serializeData(data));
            } catch (DeserializeException | IOException | SerializeException e) {
                e.printStackTrace();
            }
        }
        return dataList;
    }
}
