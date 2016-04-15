package com.flipkart.batching.persistence;

import com.flipkart.batching.Batch;
import com.flipkart.batching.Data;
import com.flipkart.batching.tape.FileObjectQueue;
import com.flipkart.batching.tape.InMemoryObjectQueue;
import com.flipkart.batching.tape.ObjectQueue;

import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

/**
 * Created by kushal.sharma on 23/02/16.
 * Simple class for Tape Persistence Strategy that extends In Memory Persistence Strategy
 */

public class TapePersistenceStrategy<E extends Data> extends InMemoryPersistenceStrategy<E> {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(TapePersistenceStrategy.class);
    private FileObjectQueue.Converter<E> converter;
    private String filePath;
    private ObjectQueue<E> queueFile;

    /**
     * Initialise Tape persistence strategy using this constructor. This takes in File Path and
     * Serialization Strategy as parameters. {@link com.flipkart.batching.tape.FileObjectQueue.Converter} is
     * initialized using the provided Serialization Strategy.
     *
     * @param filePath              path to save file
     * @param serializationStrategy serialization strategy
     */

    public TapePersistenceStrategy(String filePath, final SerializationStrategy<E, ? extends Batch> serializationStrategy) {
        this.filePath = filePath;
        this.converter = new FileObjectQueue.Converter<E>() {
            @Override
            public E from(byte[] bytes) throws IOException {
                return serializationStrategy.deserializeData(bytes);
            }

            @Override
            public void toStream(E data, OutputStream bytes) throws IOException {
                serializationStrategy.serializeData(data, bytes);
            }
        };
    }

    /**
     * Returns the current Queue File
     *
     * @return queue file
     */
    public ObjectQueue<E> getQueueFile() {
        return queueFile;
    }

    /**
     * Adds a collection of data objects to {@link com.flipkart.batching.tape.QueueFile}
     *
     * @param dataCollection collection of data to be added to queue file
     * @return true if queue file was edited
     */

    @Override
    public boolean add(Collection<E> dataCollection) {
        boolean isAdded = false;
        Collection<E> oldData = getData();
        for (E data : dataCollection) {
            if (null == data) {
                if (log.isErrorEnabled()) {
                    log.error("Null not expected in the data collection");
                }
            } else if (!oldData.contains(data)) {
                isAdded = true;
                queueFile.add(data);
            }
        }
        super.add(dataCollection);
        return isAdded;
    }

    @Override
    public boolean removeData(Collection<E> dataCollection) {
        super.removeData(dataCollection);
        for (E ignored : dataCollection) {
            E peekedData = queueFile.peek();
            if (null == peekedData) {
                if (log.isErrorEnabled()) {
                    log.error("Data being peeked is null in removeData");
                }
            } else if (dataCollection.contains(peekedData)) {
                queueFile.remove();
            }
        }
        return true;
    }

    @Override
    public void onInitialized() {
        if (!isInitialized()) {
            try {
                File file = new File(filePath);
                this.queueFile = new FileObjectQueue<>(file, converter);
            } catch (IOException e) {
                this.queueFile = new InMemoryObjectQueue<>();
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
