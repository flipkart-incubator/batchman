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

package com.flipkart.batching.toolbox;

import android.content.Context;

import com.flipkart.batching.Data;
import com.flipkart.batching.data.Tag;
import com.flipkart.batching.persistence.InMemoryPersistenceStrategy;
import com.flipkart.batching.persistence.SQLPersistenceStrategy;
import com.flipkart.batching.persistence.SerializationStrategy;
import com.flipkart.batching.persistence.TagBasedPersistenceStrategy;
import com.flipkart.batching.persistence.TapePersistenceStrategy;
import com.flipkart.batching.strategy.SizeTimeBatchingStrategy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * SizeTimeStrategyFactory
 */
public class SizeTimeStrategyFactory {
    public static SizeTimeBatchingStrategy createDefault(Context context, @NotNull Tag tag,
                                                         SerializationStrategy serializationStrategy) {
        return createWithTapePersistence(context, tag, serializationStrategy, 3, 10000);
    }

    public static <E extends Data> SizeTimeBatchingStrategy createWithInMemoryPersistence(@Nullable Tag tag,
                                                                                          int size,
                                                                                          long time) {
        if (tag != null) {
            InMemoryPersistenceStrategy<E> inMemoryPersistenceStrategy = new InMemoryPersistenceStrategy<>();
            TagBasedPersistenceStrategy<E> tagBatchingPersistence = new TagBasedPersistenceStrategy<>(tag, inMemoryPersistenceStrategy);
            return new SizeTimeBatchingStrategy<>(tagBatchingPersistence, size, time);
        } else {
            InMemoryPersistenceStrategy<E> inMemoryPersistenceStrategy = new InMemoryPersistenceStrategy<>();
            return new SizeTimeBatchingStrategy<>(inMemoryPersistenceStrategy, size, time);
        }
    }

    public static <E extends Data> SizeTimeBatchingStrategy createWithSQLPersistence(Context context,
                                                                                     @Nullable Tag tag,
                                                                                     @NotNull SerializationStrategy serializationStrategy,
                                                                                     String databaseName,
                                                                                     int size,
                                                                                     long time) {
        if (tag != null) {
            SQLPersistenceStrategy<E> sqlPersistenceStrategy = new SQLPersistenceStrategy<>(serializationStrategy, databaseName, context);
            TagBasedPersistenceStrategy<E> tagBatchingPersistence = new TagBasedPersistenceStrategy<>(tag, sqlPersistenceStrategy);
            return new SizeTimeBatchingStrategy<>(tagBatchingPersistence, size, time);
        } else {
            SQLPersistenceStrategy<E> sqlPersistenceStrategy = new SQLPersistenceStrategy<>(serializationStrategy, databaseName, context);
            return new SizeTimeBatchingStrategy<>(sqlPersistenceStrategy, size, time);
        }
    }

    public static <E extends Data> SizeTimeBatchingStrategy createWithTapePersistence(Context context,
                                                                                      @NotNull Tag tag,
                                                                                      @NotNull SerializationStrategy serializationStrategy,
                                                                                      int size,
                                                                                      long time) {
        TapePersistenceStrategy<E> tapePersistenceStrategy = new TapePersistenceStrategy<>(getFilePathForPersistenceStrategy(context, tag.getId()), serializationStrategy);
        TagBasedPersistenceStrategy<E> tagBatchingPersistence = new TagBasedPersistenceStrategy<>(tag, tapePersistenceStrategy);
        return new SizeTimeBatchingStrategy<>(tagBatchingPersistence, size, time);
    }

    public static <E extends Data> SizeTimeBatchingStrategy createWithTapePersistence(Context context,
                                                                                      String persistenceFileName,
                                                                                      SerializationStrategy serializationStrategy,
                                                                                      int size,
                                                                                      long time) {
        TapePersistenceStrategy<E> tapePersistenceStrategy = new TapePersistenceStrategy<>(getFilePathForPersistenceStrategy(context, persistenceFileName), serializationStrategy);
        return new SizeTimeBatchingStrategy<>(tapePersistenceStrategy, size, time);
    }

    private static String getFilePathForPersistenceStrategy(Context context, String tag) {
        return context.getCacheDir() + File.separator + tag + "PS";
    }
}