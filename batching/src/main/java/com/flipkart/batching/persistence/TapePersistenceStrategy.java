/*
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2017 Flipkart Internet Pvt. Ltd.
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

import android.support.annotation.VisibleForTesting;

import com.flipkart.batching.core.Batch;
import com.flipkart.batching.core.Data;
import com.flipkart.batching.core.SerializationStrategy;
import com.flipkart.batching.tape.FileObjectQueue;
import com.flipkart.batching.tape.InMemoryObjectQueue;
import com.flipkart.batching.tape.ObjectQueue;
import com.flipkart.batching.toolbox.LenientFileObjectQueue;
import com.flipkart.batching.toolbox.LenientQueueFile;
import com.flipkart.batching.toolbox.LogUtil;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

/**
 * TapePersistenceStrategy that extends {@link InMemoryPersistenceStrategy} Strategy is an
 * implementation of {@link PersistenceStrategy}. This strategy is used to persist data to disk.
 */
public class TapePersistenceStrategy<E extends Data> extends InMemoryPersistenceStrategy<E> implements LenientQueueFile.QueueFileErrorCallback {
    private static final String TAG = "TapePersistenceStrategy";
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
                LogUtil.log(TAG, "Null not expected in the data collection");
            } else {
                try {
                    queueFile.add(data);
                    add(data);
                    isAdded = true;
                } catch (IOException e) {
                    LogUtil.log(TAG, e.getLocalizedMessage());
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
                    LogUtil.log(TAG, e.getLocalizedMessage());
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
            createInMemoryQueueFile(e);
        }
    }

    @VisibleForTesting
    void createInMemoryQueueFile(IOException e) {
        this.queueFile = new InMemoryObjectQueue<E>();
        /*
         * Due to an exception while creating a tape queue file (which may happen due to low available memory), we are creating an in-memory queue file,
         * and adding the dataList to the in-memory queue.
         */
        for (E data: dataList) {
            try {
                this.queueFile.add(data);
            } catch (IOException e1) {
                LogUtil.log(TAG, e1.getLocalizedMessage());
            }
        }
        LogUtil.log(TAG, e.getLocalizedMessage());
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
            LogUtil.log(TAG, e.getLocalizedMessage());
        }
    }

    @Override
    public void onQueueFileOperationError(Throwable e) {
        LogUtil.log(TAG, "QueueFile {} is corrupt, gonna recreate it" + filePath);
        File file = new File(filePath);
        file.delete();
        tryCreatingQueueFile();
    }
}
