/*
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2016 Flipkart Internet Pvt. Ltd.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

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
