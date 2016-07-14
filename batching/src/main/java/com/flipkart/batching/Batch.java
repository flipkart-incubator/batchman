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

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Collection;


public class Batch<T extends Data> implements Serializable {
    @SerializedName("dataCollection")
    private DataCollection dataCollection;

    public Batch(Collection<T> dataCollection) {
        this.dataCollection = new DataCollection(dataCollection);
    }

    public Collection<T> getDataCollection() {
        return dataCollection.dataCollection;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Batch) {
            return dataCollection.equals(((Batch) o).dataCollection);
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return dataCollection == null ? 0 : dataCollection.hashCode();
    }
}
