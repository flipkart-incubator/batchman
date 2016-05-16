package com.flipkart.batching.listener;

/**
 * Interface for TrimmedBatchCallback
 */
public interface TrimmedBatchCallback {
    void onTrimmed(int oldSize, int newSize);
}
