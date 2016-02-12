package com.flipkart.data;

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

    private Tag tag;
    private Object data;
    private long eventId;

    /**
     * Constructor for Data object. This constructor takes {@link Tag} and {@link Object} as
     * parameter and generates an eventId = (System.currentTimeMillis() + System.nanoTime())
     *
     * @param tag  tag associated with data
     * @param data data object
     */

    public Data(Tag tag, Object data) {
        this.tag = tag;
        this.data = data;
        this.eventId = (System.currentTimeMillis() + System.nanoTime());
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public long getEventId() {
        return eventId;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Data) {
            return ((Data) o).getTag().equals(getTag()) && ((Data) o).getEventId() == getEventId() && ((Data) o).getData().equals(getData());
        } else {
            return super.equals(o);
        }
    }
}
