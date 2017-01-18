/*
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2017 Flipkart Internet Pvt. Ltd.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package com.flipkart.batching.toolbox;

import com.flipkart.batching.tape.QueueFile;

import java.io.File;
import java.io.IOException;

/**
 * A queue file made to ignore ArrayIndexOutOfBounds which causes OOM errors which happen once in a while due to disk corruption
 * Similar issue https://github.com/segmentio/analytics-android/pull/434
 * <p>
 * Use {@link QueueFileErrorCallback} to create a new queuefile and delete this one.
 */
@SuppressWarnings("FunctionalInterfaceClash")
public class LenientQueueFile extends QueueFile {

    private final QueueFileErrorCallback mCallback;

    /**
     * A lenient version of Queuefile
     *
     * @param file     refer super's documentation
     * @param callback This will be invoked when an error is consumed. Note that cleanup/recovery has to be done externally.
     * @throws IOException
     */
    public LenientQueueFile(File file, QueueFileErrorCallback callback) throws IOException {
        super(file);
        mCallback = callback;
    }

    private void handleError(Throwable e) {
        if (mCallback != null) mCallback.onQueueFileOperationError(e);
    }

    @Override
    public void add(byte[] data) throws IOException {
        try {
            super.add(data);
        } catch (Throwable e) {
            handleError(e);
        }
    }

    @Override
    public synchronized void add(byte[] data, int offset, int count) throws IOException {
        try {
            super.add(data, offset, count);
        } catch (Throwable e) {
            handleError(e);
        }
    }


    @Override
    public synchronized byte[] peek() throws IOException {
        try {
            return super.peek();
        } catch (Throwable e) {
            handleError(e);
        }
        return new byte[0];
    }

    @Override
    public synchronized void peek(ElementReader reader) throws IOException {
        try {
            super.peek(reader);
        } catch (Throwable e) {
            handleError(e);
        }
    }

    @Override
    public synchronized void peek(ElementVisitor visitor) throws IOException {
        try {
            super.peek(visitor);
        } catch (Throwable e) {
            handleError(e);
        }
    }

    @Override
    public synchronized void forEach(ElementReader reader) throws IOException {
        try {
            super.forEach(reader);
        } catch (Throwable e) {
            handleError(e);
        }
    }

    @Override
    public synchronized int forEach(ElementVisitor reader) throws IOException {
        try {
            return super.forEach(reader);
        } catch (Throwable e) {
            handleError(e);
        }
        return 0;
    }

    @Override
    public synchronized int size() {
        try {
            return super.size();
        } catch (Throwable e) {
            handleError(e);
        }
        return 0;
    }

    @Override
    public synchronized void remove() throws IOException {
        try {
            super.remove();
        } catch (Throwable e) {
            handleError(e);
        }
    }

    @Override
    public synchronized void remove(int n) throws IOException {
        try {
            super.remove(n);
        } catch (Throwable e) {
            handleError(e);
        }
    }

    @Override
    public synchronized void clear() throws IOException {
        try {
            super.clear();
        } catch (Throwable e) {
            handleError(e);
        }
    }

    @Override
    public synchronized void close() throws IOException {
        try {
            super.close();
        } catch (Throwable e) {
            handleError(e);
        }
    }

    public interface QueueFileErrorCallback {
        void onQueueFileOperationError(Throwable e);
    }
}
