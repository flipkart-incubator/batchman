package com.flipkart.batching.listener;

import com.flipkart.batching.Batch;

/**
 * Created by anirudh.r on 26/02/16.
 */
public interface PersistedBatchCallback<T extends Batch> {
    void onPersistFailure(T batch, Exception e);

    void onPersistSuccess(T batch);
}
