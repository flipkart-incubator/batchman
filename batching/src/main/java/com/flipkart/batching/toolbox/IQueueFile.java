package com.flipkart.batching.toolbox;

import com.flipkart.batching.exception.DeserializeException;
import com.flipkart.batching.exception.SerializeException;

import java.io.IOException;

/**
 * Created by kushal.sharma on 06/04/16.
 */
public interface IQueueFile {
    void add(byte[] data) throws SerializeException, IOException;

    void remove() throws IOException;

    byte[] peek() throws IOException, DeserializeException;

    int size();

    void close() throws IOException;
}
