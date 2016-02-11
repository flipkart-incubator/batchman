package com.flipkart.batching;

import android.os.Handler;

import com.flipkart.data.Data;
import com.flipkart.persistence.SerializationStrategy;

import java.util.Collection;

/**
 * Interface class for BatchController. A custom implementation of BatchingController must
 * implement this interface and override all it's methods.
 */

public interface BatchController {

    void addToBatch(Data data);

    Handler getHandler();

    SerializationStrategy getSerializationStrategy();
}
