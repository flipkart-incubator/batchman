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

import com.flipkart.Utils;
import com.flipkart.batching.Batch;
import com.flipkart.batching.Data;

import junit.framework.Assert;

import org.junit.Test;

import java.util.ArrayList;

public class BatchTest {

    /**
     * Test to verify the {@link Batch#getDataCollection()} method.
     */
    @Test
    public void testBatch() {
        ArrayList<Data> dataArrayList = Utils.fakeCollection(4);
        Batch<Data> batch = new Batch<>(dataArrayList);
        Assert.assertEquals(batch.getDataCollection(), dataArrayList);
    }

    /**
     * Test to verify that {@link Batch#equals(Object)} method.
     */
    @Test
    public void testBatchEquals() {
        ArrayList<Data> arrayList = Utils.fakeCollection(4);
        ArrayList<Data> arrayList1 = Utils.fakeCollection(5);
        Batch<Data> batch = new Batch<>(arrayList);
        Batch<Data> batch1 = new Batch<>(arrayList1);
        Assert.assertTrue(!batch.equals(batch1));
    }
}
