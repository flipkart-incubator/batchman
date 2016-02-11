package com.flipkart.data;

/**
 * Custom data class that extends abstract base class {@link Data}.
 * <p/>
 * This class takes {@link Tag} and {@link Object} as parameters in constructor and must
 * call the super constructor with the given parameters.
 */

public class EventData extends Data {

    /**
     * Constructor for EventData object. The constructor must call super(tag, data).
     *
     * @param tag  tag associated with data
     * @param data data object
     */

    public EventData(Tag tag, Object data) {
        super(tag, data);
    }
}
