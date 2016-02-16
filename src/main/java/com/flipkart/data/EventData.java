package com.flipkart.data;

/**
 * Created by kushal.sharma on 16/02/16.
 */
public class EventData extends Data {
    /**
     * Constructor for Data object. This constructor takes {@link Tag} and {@link Object} as
     * parameter and generates an eventId = (System.currentTimeMillis() + System.nanoTime())
     *
     * @param tag  tag associated with data
     * @param data data object
     */
    public EventData(Tag tag, Object data) {
        super(tag, data);
    }
}
