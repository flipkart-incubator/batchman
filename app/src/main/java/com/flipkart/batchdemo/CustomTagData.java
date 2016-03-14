package com.flipkart.batchdemo;

import com.flipkart.batching.data.Tag;
import com.flipkart.batching.data.TagData;
import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

/**
 * Created by kushal.sharma on 15/02/16.
 */
public class CustomTagData extends TagData {
    @SerializedName("event")
    private final JSONObject event;

    public CustomTagData(Tag tag, JSONObject event) {
        super(tag);
        this.event = event;
    }

    public JSONObject getEvent() {
        return event;
    }

    @Override
    public String toString() {
        return super.toString() + ":" + getEventId();
    }
}
