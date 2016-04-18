package com.flipkart.batching.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Tag represents a group of {@link com.flipkart.batching.Data} objects to batch together.
 * It takes a {@link String} type ID as parameter in constructor.
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
