package com.flipkart.batching;


public interface OnBatchReadyListener<E extends Data, T extends Batch<E>> {

    void onReady(BatchingStrategy<E, T> causingStrategy, T batch);
}
