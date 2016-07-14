/*
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2016 Flipkart Internet Pvt. Ltd.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package com.flipkart.batching.persistence;

import com.flipkart.batching.Batch;
import com.flipkart.batching.Data;
import com.flipkart.batching.tape.FileObjectQueue;
import com.flipkart.batching.tape.InMemoryObjectQueue;
import com.flipkart.batching.tape.ObjectQueue;
import com.flipkart.batching.toolbox.LenientFileObjectQueue;
import com.flipkart.batching.toolbox.LenientQueueFile;

import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

/**
 * TapePersistenceStrategy that extends {@link InMemoryPersistenceStrategy} Strategy is an
 * implementation of {@link PersistenceStrategy}. This strategy is used to persist data to disk.
 */
public class TapePersistenceStrategy<E extends Data> extends InMemoryPersistenceStrategy<E> implements LenientQueueFile.QueueFileErrorCallback {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(TapePersistenceStrategy.class);
    private String filePath;
    private ObjectQueue<E> queueFile;
    private FileObjectQueue.Converter<E> converter;

    /**
     * This constructor takes in a filePath and {@link SerializationStrategy} and creates an instance
     * of TapePersistenceStrategy.
     *
     * @param filePath              path where file is created
     * @param serializationStrategy serializationStrategy
     */
    public TapePersistenceStrategy(String filePath, final SerializationStrategy<E, ? extends Batch> serializationStrategy) {
        this.filePath = filePath;
        this.converter = new DataObjectConverter<>(serializationStrategy);
    }


    /**
     * This method add a collection of data to initialized {@link ObjectQueue}.
     *
     * @param dataCollection collection of data to add
     * @return true if list is edited
     */
    @Override
    public boolean add(Collection<E> dataCollection) {
        boolean isAdded = false;
        for (E data : dataCollection) {
            if (null == data) {
                if (log.isErrorEnabled()) {
                    log.error("Null not expected in the data collection");
                }
            } else {
                try {
                    queueFile.add(data);
                    add(data);
                    isAdded = true;
                } catch (IOException e) {
                    if (log.isErrorEnabled()) {
                        log.error(e.getLocalizedMessage());
                    }
                }
            }
        }
        return isAdded;
    }

    /**
     * Removes collection of data from initialized {@link ObjectQueue}.
     *
     * @param dataCollection collection of data to be removed
     */
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

    /**
     * Initializes {@link ObjectQueue} with {@link FileObjectQueue} and if there is an exception
     * while creating fileObjectQueue, {@link InMemoryObjectQueue} is initialized.
     */
    @Override
    public void onInitialized() {
        if (!isInitialized()) {
            tryCreatingQueueFile();
            syncData();
        }
        super.onInitialized();
    }

    private void tryCreatingQueueFile() {
        try {
            File file = new File(filePath);
            this.queueFile = new LenientFileObjectQueue(file, converter, this);
        } catch (IOException e) {
            this.queueFile = new InMemoryObjectQueue<E>();
            if (log.isErrorEnabled()) {
                log.error(e.getLocalizedMessage());
            }
        }
    }

    /**
     * This method syncs the InMemoryLayout with the data from {@link ObjectQueue}.
     * This happens only once at app startup while initialization.
     */
    private void syncData() {
        try {
            Collection<E> data = queueFile.peek(queueFile.size());
            super.add(data);
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getLocalizedMessage());
            }
        }
    }

    @Override
    public void onQueueFileOperationError(Throwable e) {
        if (log.isErrorEnabled()) {
            log.error("QueueFile {} is corrupt, gonna recreate it", filePath);
        }
        File file = new File(filePath);
        file.delete();
        tryCreatingQueueFile();
    }
}
