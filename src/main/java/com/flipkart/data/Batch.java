package com.flipkart.data;

import com.flipkart.batching.BatchInfo;

import java.io.Serializable;
import java.util.Collection;

/**
 * Created by kushal.sharma on 15/02/16.
 */

public class Batch implements Serializable {
    private BatchInfo batchInfo;
    private Collection<Data> dataCollection;

    public Batch(BatchInfo batchInfo, Collection<Data> dataCollection) {
        this.batchInfo = batchInfo;
        this.dataCollection = dataCollection;
    }

    public BatchInfo getBatchInfo() {
        return batchInfo;
    }

    public Collection<Data> getDataCollection() {
        return dataCollection;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Batch) {
            return batchInfo.equals(((Batch) o).batchInfo) && dataCollection.equals(((Batch) o).dataCollection);
        }
        return super.equals(o);
    }
}
