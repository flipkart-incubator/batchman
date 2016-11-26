package com.flipkart.batching_core.batch;

import com.flipkart.batching_core.BatchImpl;
import com.flipkart.batching_core.Data;

import java.util.Collection;

public class SizeTimeBatch<T extends Data> extends BatchImpl<T> {
    private int maxBatchSize;
    private long timeOut;

    public SizeTimeBatch(Collection dataCollection, int maxBatchSize, long timeOut) {
        super(dataCollection);
        this.maxBatchSize = maxBatchSize;
        this.timeOut = timeOut;
    }

    public int getMaxBatchSize() {
        return maxBatchSize;
    }

    public long getTimeOut() {
        return timeOut;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SizeTimeBatch) {
            return (((SizeTimeBatch) o).getMaxBatchSize() == maxBatchSize
                    && ((SizeTimeBatch) o).getTimeOut() == timeOut
                    && super.equals(o));

        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + maxBatchSize;
        result = 31 * result + Long.valueOf(timeOut).hashCode();
        return result;
    }
}