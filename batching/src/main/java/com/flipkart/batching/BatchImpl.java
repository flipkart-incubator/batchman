package com.flipkart.batching;

import com.google.gson.annotations.SerializedName;

import java.util.Collection;

/**
 * Created by anirudh.r on 14/07/16.
 */

public class BatchImpl<T extends Data> implements Batch<T> {

    @SerializedName("dataCollection")
    private DataCollection<T> dataCollection;

    public BatchImpl(Collection<T> dataCollection) {
        this.dataCollection = new DataCollection(dataCollection);
    }

    @Override
    public Collection<T> getDataCollection() {
        return dataCollection.dataCollection;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Batch) {
            return dataCollection.equals(((BatchImpl) o).dataCollection);
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return dataCollection == null ? 0 : dataCollection.hashCode();
    }
}
