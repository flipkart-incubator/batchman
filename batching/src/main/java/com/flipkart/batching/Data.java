package com.flipkart.batching;

import com.flipkart.batching.data.Tag;

import java.io.Serializable;

/**
 * This is an abstract base class for storing data which implements {@link Serializable}.
 * <p/>
 * A custom data class must extend this class and call the super in the constructor with
 * {@link Tag} and {@link Object} as parameters.
 *
 * @see Tag
 * @see Object
 */

public abstract class Data implements Serializable {

    private Object data;
    private long eventId;

    /**
     * Constructor for Data object. This constructor takes {@link Tag} and {@link Object} as
     * parameter and generates an eventId = (System.currentTimeMillis() + System.nanoTime())
     *
     * @param data data object
     */

    public Data(Object data) {
        this.data = data;
        this.eventId = (System.currentTimeMillis() + System.nanoTime());
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
    public long getEventId() {
        return eventId;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Data) {
            return ((Data) o).getEventId() == getEventId() && ((Data) o).getData().equals(getData());
        } else {
            return super.equals(o);
        }
    }
}
