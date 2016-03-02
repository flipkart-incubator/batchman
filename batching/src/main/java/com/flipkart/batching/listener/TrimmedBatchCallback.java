package com.flipkart.batching.listener;

/**
 * Created by anirudh.r on 26/02/16.
 */
public interface TrimmedBatchCallback {
    void onTrimmed(int oldSize, int newSize);
}
