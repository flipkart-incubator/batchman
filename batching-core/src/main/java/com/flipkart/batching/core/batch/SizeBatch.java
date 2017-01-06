package com.flipkart.batching.core.batch;


import com.flipkart.batching.core.BatchImpl;
import com.flipkart.batching.core.Data;

import java.util.Collection;
import java.util.Collections;

public class SizeBatch<T extends Data> extends BatchImpl<T> {
    public int maxBatchSize;

    public SizeBatch() {
        super(Collections.EMPTY_LIST);
    }

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