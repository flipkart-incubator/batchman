package com.flipkart.batching.persistence;

import com.flipkart.batching.Batch;
import com.flipkart.batching.Data;
import com.flipkart.batching.exception.DeserializeException;
import com.flipkart.batching.exception.SerializeException;
import com.flipkart.batching.toolbox.IQueueFile;
import com.flipkart.batching.toolbox.InMemoryQueueFile;
import com.flipkart.batching.toolbox.TapeQueueFile;

import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by kushal.sharma on 23/02/16.
 * Simple class for Tape Persistence Strategy that extends In Memory Persistence Strategy
 */

public class TapePersistenceStrategy<E extends Data> extends InMemoryPersistenceStrategy<E> {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(TapePersistenceStrategy.class);
    private String filePath;
    private IQueueFile queueFile;
    private SerializationStrategy<E, ? extends Batch> serializationStrategy;

    public TapePersistenceStrategy(String filePath, SerializationStrategy<E, ? extends Batch> serializationStrategy) {
        this.filePath = filePath;
        this.serializationStrategy = serializationStrategy;
    }

    public IQueueFile getQueueFile() {
        return queueFile;
    }

    @Override
    public boolean add(Collection<E> dataCollection) {
        boolean isAdded = false;
        for (E data : dataCollection) {
            try {
                if (null == data) {
                    if (log.isErrorEnabled()) {
                        log.error("Null not expected in the data collection");
                    }
                } else {
                    isAdded = true;
                    byte[] serializedData = serializationStrategy.serializeData(data);
                    if (null != serializedData) {
                        queueFile.add(serializedData);
                        add(data);
                    } else if (log.isErrorEnabled()) {
                        log.error("Data being serialized to null. This is something which is not expected");
                    }
                }
            } catch (IOException | SerializeException e) {
                if (log.isErrorEnabled()) {
                    log.error(e.getLocalizedMessage());
                }
            }
        }
        return isAdded;
    }

    @Override
    public void removeData(Collection<E> dataCollection) {
        Iterator<?> it = dataList.iterator();
        while (it.hasNext()) {
            if (dataCollection.contains(it.next())) {
                try {
                    queueFile.remove();
                    it.remove();
                } catch (IOException e) {
                    if (log.isErrorEnabled()) {
                        log.error(e.getLocalizedMessage());
                    }
                }
            }
        }
    }

    @Override
    public void onInitialized() {
        if (!isInitialized()) {
            try {
                File file = new File(filePath);
                this.queueFile = new TapeQueueFile(file);
            } catch (IOException e) {
                this.queueFile = new InMemoryQueueFile();
                if (log.isErrorEnabled()) {
                    log.error(e.getLocalizedMessage());
                }
            }
            syncData();
        }
        super.onInitialized();
    }

    /**
     * Very expensive operation
     */

    // Todo fix this
    private void syncData() {
        int size = queueFile.size();
        for (int i = 0; i < size; i++) {
            try {
                byte[] peekedData = queueFile.peek();
                if (null != peekedData) {
                    E data = serializationStrategy.deserializeData(peekedData);
                    queueFile.remove();
                    if (null != data) {
                        queueFile.add(peekedData);
                        add(data);
                    }
                }
            } catch (DeserializeException | IOException | SerializeException e) {
                if (log.isErrorEnabled()) {
                    log.error(e.getLocalizedMessage());
                }
            }
        }
    }
}
