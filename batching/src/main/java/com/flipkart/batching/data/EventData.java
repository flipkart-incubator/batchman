package com.flipkart.batching.data;

import com.flipkart.batching.Data;

/**
 * Created by kushal.sharma on 16/02/16.
 * Event Data Class to hold event data.
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
