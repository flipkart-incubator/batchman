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

package com.flipkart.batching;

import android.content.Context;
import android.os.Handler;

import com.flipkart.batchingcore.Batch;
import com.flipkart.batchingcore.Data;

import java.util.Collection;

/**
 * Interface class for BatchingStrategy. An implementation of BatchingStrategy must
 * implement this interface and override all it's methods.
 */

public interface BatchingStrategy<E extends Data, T extends Batch<E>> {
    /**
     * This method tells the BatchingStrategy about added data. This method should send the
     * provided {@link Collection} of {@link Data} objects to the provided implementation of
     * {@link com.flipkart.batching.persistence.PersistenceStrategy}.
     *
     * @param dataCollection collection of {@link Data} objects
     */

    void onDataPushed(Collection<E> dataCollection);

    /**
     * This method fires the {@link OnBatchReadyListener} when a batch is ready, depending on the
     * provided BatchingStrategy.
     *
     * @param forced boolean type if isForced
     */

    void flush(boolean forced);

    /**
     * This method returns false if {@link Context}, {@link BatchController}, {@link OnBatchReadyListener}
     * and {@link Handler} are not initialized and true if initialized. Typically, onInitialized should
     * be called only once and the value of isInitialized must be set to true after initializing everything.
     *
     * @return boolean, true if initialized and false if not
     */

    boolean isInitialized();

    /**
     * Instance of {@link BatchController}, {@link Context}, {@link OnBatchReadyListener},
     * and {@link Handler} from {@link BatchController}.
     *
     * @param context              context
     * @param onBatchReadyListener instance of {@link OnBatchReadyListener}
     * @param handler              instance of {@link Handler}
     */

    void onInitialized(Context context, OnBatchReadyListener<E, T> onBatchReadyListener, Handler handler);
}
