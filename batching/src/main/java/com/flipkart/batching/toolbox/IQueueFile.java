package com.flipkart.batching.toolbox;

import com.flipkart.batching.exception.DeserializeException;
import com.flipkart.batching.exception.SerializeException;

import java.io.IOException;
import java.util.Collection;

/**
 * Created by kushal.sharma on 06/04/16.
 */
public interface IQueueFile<E> {
    void add(E data) throws SerializeException, IOException;

    void remove() throws IOException;

    E peek() throws IOException, DeserializeException;

    int size();

    void close() throws IOException;

    Collection<E> peek(int size);
}
