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

import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Created by kushal.sharma on 29/03/16.
 */

public class ComboStrategyFactory {
    public static final int IN_MEMORY_PERSISTENCE = 0;
    public static final int SQL_PERSISTENCE = 1;
    public static final int TAPE_PERSISTENCE = 2;

    public static ComboBatchingStrategy createDefault(Context context, @Nullable Tag tag,
                                                      @Nullable SerializationStrategy serializationStrategy) {
        return create(context, TAPE_PERSISTENCE, tag, serializationStrategy, 3, 10000);
    }

    public static <E extends Data> ComboBatchingStrategy create(Context context, int persistenceStrategy,
                                                                @Nullable Tag tag,
                                                                @Nullable SerializationStrategy serializationStrategy,
                                                                int size, long time) {
        switch (persistenceStrategy) {
            case IN_MEMORY_PERSISTENCE:
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
            case SQL_PERSISTENCE:
                if (tag != null && serializationStrategy != null) {
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
            case TAPE_PERSISTENCE:
                if (tag != null && serializationStrategy != null) {
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
        return null;
    }

    private static String getFilePathForPersistenceStrategy(Context context, String tag) {
        return context.getCacheDir() + File.separator + tag + "PS";
    }
}


