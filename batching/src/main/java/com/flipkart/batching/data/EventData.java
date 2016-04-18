package com.flipkart.batching.data;

import com.flipkart.batching.Data;

/**
 * EventData class that extends {@link Data}.
 */

public class EventData extends Data {
    public EventData() {
        super();
    }

    @Override
    public String toString() {
        return super.toString() + ":" + getEventId();
    }
}
