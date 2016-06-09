package com.flipkart.batching.toolbox;

import com.flipkart.batching.tape.FileObjectQueue;

import java.io.File;
import java.io.IOException;

/**
 * A wrapper over {@link LenientFileObjectQueue}
 */

public class LenientFileObjectQueue<T> extends FileObjectQueue<T> {
    public LenientFileObjectQueue(File file, Converter converter, LenientQueueFile.QueueFileErrorCallback errorCallback) throws IOException {
        super(file, new LenientQueueFile(file, errorCallback), converter);
    }
}
