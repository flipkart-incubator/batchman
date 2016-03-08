package com.flipkart.batching.data;

import com.flipkart.batching.Data;
import com.google.gson.annotations.SerializedName;

/**
 * Created by kushal.sharma on 24/02/16.
 * Tag Data Class.
 */

public class TagData extends Data {

    @SerializedName("tag")
    private final Tag tag;

    public TagData(Tag tag) {
        super();
        this.tag = tag;
    }

    public Tag getTag() {
        return tag;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TagData) {
            return ((TagData) o).getTag().equals(tag) && super.equals(o);
        } else {
            return super.equals(o);
        }
    }
}
