package com.flipkart.batching.core.batch;

import androidx.annotation.Keep;

import com.flipkart.batching.core.BatchImpl;
import com.flipkart.batching.core.Data;

import java.util.Collection;

@Keep
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