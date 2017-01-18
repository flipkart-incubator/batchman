/*
 * Copyright 2012 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flipkart.batching.tape;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * Base queue class, implements common functionality for a QueueFile-backed
 * queue manager.  This class is not thread safe; instances should be kept
 * thread-confined.
 * <p/>
 * The {@link #add(Object)}, {@link #peek()}, {@link #remove()}, and
 * {@link #setListener(Listener)} methods may throw a
 * {@link FileException} if the underlying {@link QueueFile} experiences an
 * {@link IOException}.
 *
 * @param <T> The type of elements in the queue.
 */
public class FileObjectQueue<T> implements ObjectQueue<T> {
    final Converter<T> converter;
    /**
     * Backing storage implementation.
     */
    private final QueueFile queueFile;
    /**
     * Reusable byte output buffer.
     */
    private final DirectByteArrayOutputStream bytes = new DirectByteArrayOutputStream();
    /**
     * Keep file around for error reporting.
     */
    private final File file;
    private Listener<T> listener;

    public FileObjectQueue(File file, QueueFile queueFile, Converter<T> converter) throws IOException {
        this.file = file;
        this.converter = converter;
        this.queueFile = queueFile;
    }

    protected QueueFile createQueueFile(File file) throws IOException {
        return new QueueFile(file);
    }

    @Override
    public int size() {
        return queueFile.size();
    }

    @Override
    public final void add(T entry) throws IOException {
        bytes.reset();
        converter.toStream(entry, bytes);
        queueFile.add(bytes.getArray(), 0, bytes.size());
        if (listener != null) listener.onAdd(this, entry);
    }

    @Override
    public T peek() throws IOException {
        byte[] bytes = queueFile.peek();
        if (bytes == null) return null;
        return converter.from(bytes);
    }

    /**
     * Reads up to {@code max} entries from the head of the queue without removing the entries.
     * If the queue's {@link #size()} is less than {@code max} then only {@link #size()} entries
     * are read.
     */
    @Override
    public List<T> peek(final int max) throws IOException {
        final List<T> entries = new ArrayList<>(max);
        queueFile.forEach(new QueueFile.ElementVisitor() {
            int count;

            @Override
            public boolean read(InputStream in, int length) throws IOException {
                byte[] data = new byte[length];
                in.read(data, 0, length);

                entries.add(converter.from(data));
                return ++count < max;
            }
        });
        return unmodifiableList(entries);
    }

    public List<T> asList() throws IOException {
        return peek(size());
    }

    @Override
    public final void remove() throws IOException {
        queueFile.remove();
        if (listener != null) listener.onRemove(this);
    }


    @Override
    public void remove(int n) throws IOException {
        queueFile.remove(n);
        if (listener != null) {
            for (int i = 0; i < n; i++) {
                listener.onRemove(this);
            }
        }
    }

    @Override
    public final void close() throws IOException {
        queueFile.close();
    }

    @Override
    public void setListener(final Listener<T> listener) {
        if (listener != null) {
            try {
                queueFile.forEach(new QueueFile.ElementVisitor() {
                    @Override
                    public boolean read(InputStream in, int length) throws IOException {
                        byte[] data = new byte[length];
                        in.read(data, 0, length);

                        listener.onAdd(FileObjectQueue.this, converter.from(data));
                        return true;
                    }
                });
            } catch (IOException e) {
                throw new FileException("Unable to iterate over QueueFile contents.", e, file);
            }
        }
        this.listener = listener;
    }

    /**
     * Convert a byte stream to and from a concrete type.
     *
     * @param <T> Object type.
     */
    public interface Converter<T> {
        /**
         * Converts bytes to an object.
         */
        T from(byte[] bytes) throws IOException;

        /**
         * Converts o to bytes written to the specified stream.
         */
        void toStream(T o, OutputStream bytes) throws IOException;
    }

    /**
     * Enables direct access to the internal array. Avoids unnecessary copying.
     */
    private static class DirectByteArrayOutputStream extends ByteArrayOutputStream {
        public DirectByteArrayOutputStream() {
            super();
        }

        /**
         * Gets a reference to the internal byte array.  The {@link #size()} method indicates how many
         * bytes contain actual data added since the last {@link #reset()} call.
         */
        public byte[] getArray() {
            return buf;
        }
    }
}
