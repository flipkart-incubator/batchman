package com.flipkart.batching;


import java.util.Collection;

/**
 * Interface for OnBatchReadyListener. {@link #onReady(BatchingStrategy, BatchInfo, Collection) < Data >)} is called whenever a batch
 * is ready to served with ArrayList of data as it's only parameter.\
 */

public interface OnBatchReadyListener<E extends Data, T extends Batch<E>> {

    /**
     * This method is called when a batch is ready.
     *
     * @param causingStrategy the batching strategy which generated this batch of dataCollection
     * @param batchInfo       an object which describes the generated batch
     * @param dataCollection  collection of {@link Data} objects
     */
    void onReady(BatchingStrategy<E, T> causingStrategy, T batch);
}
