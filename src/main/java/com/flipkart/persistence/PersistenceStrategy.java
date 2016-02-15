package com.flipkart.persistence;

import com.flipkart.data.Data;

import java.util.Collection;

/**
 * Interface for PersistenceStrategy. A persistence strategy must implement this interface
 * and override all it's methods. Persistence strategy is responsible for persisting the.
 */

public interface PersistenceStrategy {

    /**
     * This method tells the persistence strategy about the added {@link Collection} of {@link Data}
     * and persist it according to the provided implementation of persistenceStrategy.
     *
     * @param dataCollection collection of {@link Data} objects
     */
    void add(Collection<Data> dataCollection);

    /**
     * This method returns {@link Collection} of persisted {@link Data} objects.
     *
     * @return collection of {@link Data} objects
     */
    Collection<Data> getData();

    /**
     * This method removes the provided {@link Collection} of {@link Data} objects from
     * the provided implementation of {@link PersistenceStrategy}.
     *
     * @param dataCollection collection of {@link Data} objects
     */
    void removeData(Collection<Data> dataCollection);

    void onInitialized();

}
