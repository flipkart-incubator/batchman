package com.flipkart.batching.persistence;

import com.flipkart.batching.Batch;
import com.flipkart.batching.Data;
import com.flipkart.batching.exception.DeserializeException;
import com.flipkart.batching.exception.SerializeException;
import com.flipkart.batching.tape.CustomObjectQueue;
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
    FileObjectQueue.Converter<E> converter;
    private String filePath;
    private CustomObjectQueue<E> queueFile;

    public TapePersistenceStrategy(String filePath, final SerializationStrategy<E, ? extends Batch> serializationStrategy) {
        this.filePath = filePath;
        this.converter = new FileObjectQueue.Converter<E>() {
            @Override
            public E from(byte[] bytes) throws IOException {
                try {
                    return serializationStrategy.deserializeData(bytes);
                } catch (DeserializeException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void toStream(E o, OutputStream bytes) throws IOException {
                try {
                    byte[] data = serializationStrategy.serializeData(o);
                    bytes.write(data);
                } catch (SerializeException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public ObjectQueue<E> getQueueFile() {
        return queueFile;
    }

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
                return true;
            }
        }
        return false;
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
