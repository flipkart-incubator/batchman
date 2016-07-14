/*
 * Copyright 2016 Flipkart Internet Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
