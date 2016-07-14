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

package com.flipkart.batchdemo;

import com.flipkart.batching.data.Tag;
import com.flipkart.batching.data.TagData;
import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

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
