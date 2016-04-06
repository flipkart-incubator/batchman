package com.flipkart.batching.toolbox;

import com.flipkart.batching.exception.DeserializeException;
import com.flipkart.batching.exception.SerializeException;

import java.io.File;
import java.io.IOException;

/**
 * Created by kushal.sharma on 06/04/16.
 */

public class TapeQueueFile implements IQueueFile {
    com.squareup.tape.QueueFile queueFile;

    public TapeQueueFile(File file) throws IOException {
        queueFile = new com.squareup.tape.QueueFile(file);
    }

    @Override
    public void add(byte[] data) throws SerializeException, IOException {
        queueFile.add(data);
    }

    @Override
    public void remove() throws IOException {
        queueFile.remove();
    }

    @Override
    public byte[] peek() throws IOException, DeserializeException {
        return queueFile.peek();
    }

    @Override
    public int size() {
        return queueFile.size();
    }

    @Override
    public void close() throws IOException {
        queueFile.close();
    }
}
