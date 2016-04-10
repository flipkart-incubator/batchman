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
        Collection<E> oldData = getData();
        for (E data : dataCollection) {
            try {
                if (null == data) {
                    if (log.isErrorEnabled()) {
                        log.error("Null not expected in the data collection");
                    }
                } else if (!oldData.contains(data)) {
                    isAdded = true;
                    queueFile.add(data);
                }
            } catch (IOException | SerializeException e) {
                if (log.isErrorEnabled()) {
                    log.error(e.getLocalizedMessage());
                }
            }
        }
        super.add(dataCollection);
        return isAdded;
    }

    @Override
    public void removeData(Collection<E> dataCollection) {
        super.removeData(dataCollection);
        for (E ignored : dataCollection) {
            try {
                E peekedData = (E) queueFile.peek();
                if (null == peekedData) {
                    if (log.isErrorEnabled()) {
                        log.error("Data being peeked is null in removeData");
                    }
                } else if (dataCollection.contains(peekedData)) {
                    queueFile.remove();
                }
            } catch (DeserializeException | IOException e) {
                if (log.isErrorEnabled()) {
                    log.error(e.getLocalizedMessage());
                }
            }
        }
    }

    @Override
    public void onInitialized() {
        if (!isInitialized()) {
            try {
                File file = new File(filePath);
                this.queueFile = new TapeQueueFile(file, serializationStrategy);
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

    private void syncData() {
        super.add(getAllDataFromTapeQueue());
    }

    /**
     * Very expensive operation
     *
     * @return collection of data
     */
    private Collection<E> getAllDataFromTapeQueue() {
        return queueFile.peek(queueFile.size());
    }
}
