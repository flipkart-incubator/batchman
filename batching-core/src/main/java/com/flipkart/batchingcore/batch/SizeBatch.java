package com.flipkart.batchingcore.batch;


import com.flipkart.batchingcore.BatchImpl;
import com.flipkart.batchingcore.Data;

import java.util.Collection;

public class SizeBatch<T extends Data> extends BatchImpl<T> {
    private int maxBatchSize;

    public SizeBatch(Collection dataCollection, int maxBatchSize) {
        super(dataCollection);
        this.maxBatchSize = maxBatchSize;
    }

    public int getMaxBatchSize() {
        return maxBatchSize;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SizeBatch) {
            return ((SizeBatch) o).getMaxBatchSize() == maxBatchSize && super.equals(o);
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + maxBatchSize;
    }
}