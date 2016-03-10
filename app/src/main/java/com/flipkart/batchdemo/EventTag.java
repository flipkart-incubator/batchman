package com.flipkart.batchdemo;

import com.flipkart.batching.data.Tag;
import com.google.gson.annotations.SerializedName;

/**
 * Created by kushal.sharma on 15/02/16.
 */
public class EventTag extends Tag {

    @SerializedName("url")
    public String url;

    public EventTag(String id, String url) {
        super(id);
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
