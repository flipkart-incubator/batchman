package com.flipkart.batching;

import android.os.Handler;

import com.flipkart.batching.persistence.SerializationStrategy;

import java.util.Collection;

/**
 * Interface class for BatchController. An implementation of BatchController must
 * implement this interface and override all it's methods.
 */

public interface BatchController<E extends Data, T extends Batch<E>> {
    /**
     * This method takes {@link Data} type {@link Collection} as parameter and notifies the provided
     * {@link BatchingStrategy} about the added data.
     *
     * @param dataCollection collection of {@link Data}
     */

    void addToBatch(Collection<E> dataCollection);

    /**
     * This method returns the initialized {@link Handler}.
     *
     * @return handler
     */

    Handler getHandler();

    /**
     * This method returns the initialized {@link SerializationStrategy}.
     *
     * @return serializationStrategy
     */

    SerializationStrategy<E, T> getSerializationStrategy();
}
