package com.flipkart.batching.toolbox;

import com.flipkart.batching.exception.DeserializeException;
import com.flipkart.batching.exception.SerializeException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by kushal.sharma on 06/04/16.
 */

public class InMemoryQueueFile implements IQueueFile {
    InMemoryObjectQueue inMemoryObjectQueue;

    public InMemoryQueueFile() {
        this.inMemoryObjectQueue = new InMemoryObjectQueue<>();
    }


    @Override
    public void add(Object data) throws SerializeException, IOException {
        inMemoryObjectQueue.add(data);
    }

    @Override
    public void remove() throws IOException {
        inMemoryObjectQueue.remove();
    }

    @Override
    public Object peek() throws IOException, DeserializeException {
        return inMemoryObjectQueue.peek();
    }


    @Override
    public int size() {
        return inMemoryObjectQueue.size();
    }

    @Override
    public void close() throws IOException {
        // Nothing to close.
    }

    @Override
    public Collection peek(int size) {
        return new ArrayList();
    }
}
