/*
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2016 Flipkart Internet Pvt. Ltd.
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

package com.flipkart.batching.persistence;

import com.flipkart.batching.tape.FileObjectQueue;
import com.flipkart.batching_core.Batch;
import com.flipkart.batching_core.Data;
import com.flipkart.batching_core.SerializationStrategy;

import java.io.IOException;
import java.io.OutputStream;

public class DataObjectConverter<E extends Data> implements FileObjectQueue.Converter<E> {
    private SerializationStrategy<E, ? extends Batch> serializationStrategy;

    public DataObjectConverter(SerializationStrategy<E, ? extends Batch> serializationStrategy) {
        this.serializationStrategy = serializationStrategy;
    }

    @Override
    public E from(byte[] bytes) throws IOException {
        return serializationStrategy.deserializeData(bytes);

    }

    @Override
    public void toStream(E data, OutputStream bytes) throws IOException {
        bytes.write(serializationStrategy.serializeData(data));
    }
}