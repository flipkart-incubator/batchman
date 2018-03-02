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

package com.flipkart.batching.core;

import android.support.annotation.Keep;

import java.util.Collection;

/**
 * A simple implementation of Batch interface
 */
@Keep
public class BatchImpl<T extends Data> implements Batch<T> {

    public DataCollection<T> dataCollection;

    public BatchImpl(Collection<T> dataCollection) {
        this.dataCollection = new DataCollection<>(dataCollection);
    }

    @Override
    public Collection<T> getDataCollection() {
        return dataCollection.dataCollection;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Batch) {
            return dataCollection.equals(((BatchImpl) o).dataCollection);
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return dataCollection == null ? 0 : dataCollection.hashCode();
    }
}