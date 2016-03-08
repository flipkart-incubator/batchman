package com.flipkart.batching.data;

import com.flipkart.batching.Data;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Tag is a simple class to store information about the {@link Data} object.
 * A tag implements {@link Serializable} and consists of Id, Priority and Url.
 * <p/>
 */

public class Tag implements Serializable {

    @SerializedName("id")
    private String id;

    public Tag(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Tag) {
            return id.equals(((Tag) o).getId());
        } else {
            return super.equals(o);
        }
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
