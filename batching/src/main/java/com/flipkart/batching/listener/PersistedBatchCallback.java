package com.flipkart.batching.listener;

import com.flipkart.batching.Batch;

/**
 * Persisted Batch Callback
 */
public interface PersistedBatchCallback<T extends Batch> {
    void onPersistFailure(T batch, Exception e);

    void onPersistSuccess(T batch);

    void onFinish();
}
