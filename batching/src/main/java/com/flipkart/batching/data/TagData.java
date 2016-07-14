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

package com.flipkart.batching.data;

import com.flipkart.batching.Data;
import com.google.gson.annotations.SerializedName;

/**
 * TagData Class that extends {@link Data}.
 * It takes {@link Tag} as a parameter in constructor.
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

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + (getTag() == null ? 0 : getTag().hashCode());
    }
}
