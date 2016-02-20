package com.flipkart.batching;

import java.io.Serializable;

/**
 * An interface for saving batch info. A {@link BatchingStrategy} must have a class that extends
 * from this interface and store info about the batching strategy used to batch the data.
 * <p/>
 * For Example :
 * <p/>
 * {@link SizeBatchingStrategy} contains {@link com.flipkart.batching.SizeBatchingStrategy.SizeBatchInfo}
 * which extends this interface and stores the maxBatchSize.
 */

public interface BatchInfo extends Serializable {
}
