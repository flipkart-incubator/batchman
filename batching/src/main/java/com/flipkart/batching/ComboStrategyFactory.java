package com.flipkart.batching;

import android.content.Context;

import com.flipkart.batching.data.Tag;
import com.flipkart.batching.persistence.InMemoryPersistenceStrategy;
import com.flipkart.batching.persistence.SQLPersistenceStrategy;
import com.flipkart.batching.persistence.SerializationStrategy;
import com.flipkart.batching.persistence.TagBasedPersistenceStrategy;
import com.flipkart.batching.persistence.TapePersistenceStrategy;
import com.flipkart.batching.strategy.ComboBatchingStrategy;
import com.flipkart.batching.strategy.SizeBatchingStrategy;
import com.flipkart.batching.strategy.TimeBatchingStrategy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Created by kushal.sharma on 29/03/16.
 */

public class ComboStrategyFactory {
    public static ComboBatchingStrategy createDefault(Context context, @Nullable Tag tag,
                                                      SerializationStrategy serializationStrategy) {
        return createWithTapePersistence(context, tag, serializationStrategy, 3, 10000);
    }

    public static <E extends Data> ComboBatchingStrategy createWithInMemoryPersistence(Context context, @Nullable Tag tag,
                                                                                       @Nullable SerializationStrategy serializationStrategy,
                                                                                       int size, long time) {
        if (tag != null) {
            InMemoryPersistenceStrategy<E> inMemoryPersistenceStrategy = new InMemoryPersistenceStrategy<>();
            TagBasedPersistenceStrategy<E> tagBatchingPersistence = new TagBasedPersistenceStrategy<>(tag, inMemoryPersistenceStrategy);
            SizeBatchingStrategy<E> sizeBatchingStrategy = new SizeBatchingStrategy<>(size, tagBatchingPersistence);
            TimeBatchingStrategy<E> timeBatchingStrategy = new TimeBatchingStrategy<>(time, tagBatchingPersistence);
            return new ComboBatchingStrategy(sizeBatchingStrategy, timeBatchingStrategy);

        } else {
            InMemoryPersistenceStrategy<E> inMemoryPersistenceStrategy = new InMemoryPersistenceStrategy<>();
            SizeBatchingStrategy<E> sizeBatchingStrategy = new SizeBatchingStrategy<>(size, inMemoryPersistenceStrategy);
            TimeBatchingStrategy<E> timeBatchingStrategy = new TimeBatchingStrategy<>(time, inMemoryPersistenceStrategy);
            return new ComboBatchingStrategy(sizeBatchingStrategy, timeBatchingStrategy);
        }
    }

    public static <E extends Data> ComboBatchingStrategy createWithSQLPersistence(Context context, @Nullable Tag tag,
                                                                                  @NotNull SerializationStrategy serializationStrategy,
                                                                                  int size, long time) {
        if (tag != null) {
            SQLPersistenceStrategy<E> sqlPersistenceStrategy = new SQLPersistenceStrategy<>(serializationStrategy, "batching", context);
            TagBasedPersistenceStrategy<E> tagBatchingPersistence = new TagBasedPersistenceStrategy<>(tag, sqlPersistenceStrategy);
            SizeBatchingStrategy<E> sizeBatchingStrategy = new SizeBatchingStrategy<>(size, tagBatchingPersistence);
            TimeBatchingStrategy<E> timeBatchingStrategy = new TimeBatchingStrategy<>(time, tagBatchingPersistence);
            return new ComboBatchingStrategy(sizeBatchingStrategy, timeBatchingStrategy);

        } else {
            SQLPersistenceStrategy<E> sqlPersistenceStrategy = new SQLPersistenceStrategy<>(serializationStrategy, "batching", context);
            SizeBatchingStrategy<E> sizeBatchingStrategy = new SizeBatchingStrategy<>(size, sqlPersistenceStrategy);
            TimeBatchingStrategy<E> timeBatchingStrategy = new TimeBatchingStrategy<>(time, sqlPersistenceStrategy);
            return new ComboBatchingStrategy(sizeBatchingStrategy, timeBatchingStrategy);
        }
    }

    public static <E extends Data> ComboBatchingStrategy createWithTapePersistence(Context context, @Nullable Tag tag,
                                                                                   @NotNull SerializationStrategy serializationStrategy,
                                                                                   int size, long time) {
        if (tag != null) {
            TapePersistenceStrategy<E> tapePersistenceStrategy = new TapePersistenceStrategy<>(getFilePathForPersistenceStrategy(context, tag.getId()), serializationStrategy);
            TagBasedPersistenceStrategy<E> tagBatchingPersistence = new TagBasedPersistenceStrategy<>(tag, tapePersistenceStrategy);
            SizeBatchingStrategy<E> sizeBatchingStrategy = new SizeBatchingStrategy<>(size, tagBatchingPersistence);
            TimeBatchingStrategy<E> timeBatchingStrategy = new TimeBatchingStrategy<>(time, tagBatchingPersistence);
            return new ComboBatchingStrategy(sizeBatchingStrategy, timeBatchingStrategy);

        } else {
            TapePersistenceStrategy<E> tapePersistenceStrategy = new TapePersistenceStrategy<>(getFilePathForPersistenceStrategy(context, tag.getId()), serializationStrategy);
            SizeBatchingStrategy<E> sizeBatchingStrategy = new SizeBatchingStrategy<>(size, tapePersistenceStrategy);
            TimeBatchingStrategy<E> timeBatchingStrategy = new TimeBatchingStrategy<>(time, tapePersistenceStrategy);
            return new ComboBatchingStrategy(sizeBatchingStrategy, timeBatchingStrategy);
        }
    }

    private static String getFilePathForPersistenceStrategy(Context context, String tag) {
        return context.getCacheDir() + File.separator + tag + "PS";
    }
}


