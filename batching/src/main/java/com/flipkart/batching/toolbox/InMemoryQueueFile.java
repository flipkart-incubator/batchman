package com.flipkart.batching.toolbox;

import com.flipkart.batching.exception.DeserializeException;
import com.flipkart.batching.exception.SerializeException;

import java.io.IOException;
import java.util.LinkedList;

/**
 * Created by kushal.sharma on 06/04/16.
 */

public class InMemoryQueueFile implements IQueueFile {
    LinkedList<byte[]> inMemoryQueue;

    public InMemoryQueueFile() {
        this.inMemoryQueue = new LinkedList<>();
    }

    @Override
    public void add(byte[] data) throws SerializeException, IOException {
        inMemoryQueue.addLast(data);
    }

    @Override
    public void remove() throws IOException {
        inMemoryQueue.removeFirst();
    }

    @Override
    public byte[] peek() throws IOException, DeserializeException {
        return inMemoryQueue.getFirst();
    }

    @Override
    public int size() {
        return inMemoryQueue.size();
    }

    @Override
    public void close() throws IOException {
        // Nothing to close.
    }
}
