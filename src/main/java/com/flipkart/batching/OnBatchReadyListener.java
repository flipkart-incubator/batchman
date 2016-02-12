package com.flipkart.batching;


import com.flipkart.data.Data;

import java.util.Collection;

/**
 * Interface for OnBatchReadyListener. {@link #onReady(Collection< Data >)} is called whenever a batch
 * is ready to served with ArrayList of data as it's only parameter.\
 */

public interface OnBatchReadyListener {

    void onReady(Collection<Data> data);
}
