package com.flipkart.batching.tape;

import com.flipkart.batching.BuildConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

/**
 * Test for {@link InMemoryObjectQueue}
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class InMemoryObjectQueueTest {

    /**
     * Test for {@link InMemoryObjectQueue#remove()} )
     */
    @Test
    public void testRemove() {
        InMemoryObjectQueue<String> inMemoryObjectQueue = new InMemoryObjectQueue<>();

        ArrayList<String> arrayList = new ArrayList<>(4);
        arrayList.add("test1");
        arrayList.add("test2");
        arrayList.add("test3");
        arrayList.add("test4");

        for (String sample : arrayList) {
            inMemoryObjectQueue.add(sample);
        }

        //remove all the 4 elements added to the queue
        inMemoryObjectQueue.remove(arrayList.size());

        //try removing another element from the queue.
        inMemoryObjectQueue.remove();
    }
}
