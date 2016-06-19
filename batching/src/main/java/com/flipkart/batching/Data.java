package com.flipkart.batching;

import com.flipkart.batching.data.Tag;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * This is an abstract base class for storing data which implements {@link Serializable}.
 * <p>
 * A custom data class must extend this class and call the super in the constructor with
 * {@link Tag} and {@link Object} as parameters.
 *
 * @see Tag
 * @see Object
 */

public abstract class Data implements Serializable {
    @SerializedName("eventId")
    private long eventId;

    /**
     * Constructor for Data object. This constructor takes {@link Tag} and {@link Object} as
     * parameter and generates an eventId = (System.currentTimeMillis() + System.nanoTime())
     */

    public Data() {
        this.eventId = System.currentTimeMillis() + System.nanoTime();
    }

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Data) {
            return ((Data) o).getEventId() == getEventId();
        } else {
            return super.equals(o);
        }
    }

    @Override
    public int hashCode() {
        return Long.valueOf(eventId).hashCode();
    }
}
