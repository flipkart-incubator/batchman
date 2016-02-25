package com.flipkart.batching;

import java.io.Serializable;
import java.util.Collection;


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
