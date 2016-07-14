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

import junit.framework.Assert;

import org.junit.Test;

public class DataTest {


    /**
     * Test to verify {@link Data#equals(Object)}
     */
    @Test
    public void testEqualsData() {
        Data data = new EventData();
        Assert.assertTrue(!data.equals("e"));
    }

    /**
     * Test to verify {@link TagData}
     */
    @Test
    public void testTagData() {
        Tag AD_TAG = new Tag("ADS");
        TagData tagData = new TagData(AD_TAG);
        Assert.assertTrue(tagData.getTag() == AD_TAG);
    }

    /**
     * Test to verify the equals method in {@link TagData}
     */
    @Test
    public void testTagEqualsData() {
        Tag AD_TAG = new Tag("ADS");
        Tag DEBUG_TAG = new Tag("DEBUG");
        TagData adsTagData = new TagData(AD_TAG);
        TagData debugTagData = new TagData(DEBUG_TAG);
        Assert.assertTrue(!adsTagData.equals(debugTagData));
        Assert.assertTrue(!adsTagData.equals(""));
    }
}
