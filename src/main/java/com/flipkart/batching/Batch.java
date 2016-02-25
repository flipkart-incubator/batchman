package com.flipkart.batching;

import com.flipkart.batching.strategy.SizeBatchingStrategy;

import java.io.Serializable;
import java.util.Collection;

/**
 * An interface for saving batch info. A {@link BatchingStrategy} must have a static class that
 * implements from this interface and store info about the batching strategy used to batch the data.
 * <p/>
 * For Example :
 * <p/>
 * {@link SizeBatchingStrategy} contains {@link SizeBatchingStrategy.SizeBatchInfo}
 * which extends this interface and stores the maxBatchSize.
 */

public class Batch<T extends Data> implements Serializable {
    private Collection<T> dataCollection;

    public Batch(Collection<T> dataCollection) {
        this.dataCollection = dataCollection;
    }


    public Collection<T> getDataCollection() {
        return dataCollection;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Batch) {
            return dataCollection.equals(((Batch) o).dataCollection);
        }
        return super.equals(o);
    }
}
