package com.flipkart.batching.toolbox;

import com.flipkart.batching.Data;
import com.flipkart.batching.exception.DeserializeException;
import com.flipkart.batching.exception.SerializeException;
import com.flipkart.batching.persistence.SerializationStrategy;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by kushal.sharma on 06/04/16.
 */
public class InMemoryQueueFile implements QueueFile {
    ArrayList<byte[]> inMemoryQueue;
    SerializationStrategy serializationStrategy;

    public InMemoryQueueFile(SerializationStrategy serializationStrategy) {
        this.inMemoryQueue = new ArrayList<>();
        this.serializationStrategy = serializationStrategy;
    }

    @Override
    public void addToQueue(Data data) throws SerializeException, IOException {
        inMemoryQueue.add(serializationStrategy.serializeData(data));
    }

    @Override
    public boolean removeFromQueue() throws IOException {
        if (!inMemoryQueue.isEmpty()) {
            inMemoryQueue.remove(0);
            return true;
        }
        return false;
    }

    @Override
    public Data peekFromQueue() throws IOException, DeserializeException {
        if (!inMemoryQueue.isEmpty()) {
            return serializationStrategy.deserializeData(inMemoryQueue.get(0));
        }
        return null;
    }
}
