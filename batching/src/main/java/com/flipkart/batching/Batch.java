package com.flipkart.batching;

import java.io.Serializable;
import java.util.Collection;


public class Batch<T extends Data> implements Serializable {

    @SerializedName("dataCollection")
    private DataCollection dataCollection;

    public Batch(Collection<T> dataCollection) {
        this.dataCollection = new DataCollection(dataCollection);
    }

    public Collection<T> getDataCollection() {
        return dataCollection.dataCollection;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Batch) {
            return dataCollection.equals(((Batch) o).dataCollection);
        }
        return super.equals(o);
    }

}
