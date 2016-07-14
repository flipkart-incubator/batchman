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
