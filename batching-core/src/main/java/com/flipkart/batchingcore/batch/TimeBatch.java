package com.flipkart.batchingcore.batch;

import com.flipkart.batchingcore.BatchImpl;
import com.flipkart.batchingcore.Data;

import java.util.Collection;

public class TimeBatch<D extends Data> extends BatchImpl<D> {
    private long timeOut;

    public TimeBatch(Collection<D> dataCollection, long timeOut) {
        super(dataCollection);
        this.timeOut = timeOut;
    }

    public long getTimeOut() {
        return timeOut;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TimeBatch) {
            return ((TimeBatch) o).getTimeOut() == timeOut && super.equals(o);
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Long.valueOf(timeOut).hashCode();
    }
}