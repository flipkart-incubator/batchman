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

import junit.framework.Assert;

import org.junit.Test;

public class TagTest {

    /**
     * Test to verify {@link Tag#getId()}
     */
    @Test
    public void setTag() {
        Tag tag = new Tag("Ads");
        Assert.assertTrue(tag.getId().equals("Ads"));

        Tag tag1 = new Tag("Ads");
        tag1.setId("Business");
        Assert.assertTrue(tag1.getId().equals("Business"));
    }

    /**
     * Test to verify {@link Tag#equals(Object)}
     */
    @Test
    public void testEquals() {
        Tag tag = new Tag("Ads");
        Tag tag1 = new Tag("Business");

        String string = "123";
        Assert.assertTrue(!tag.equals(tag1));
        Assert.assertTrue(!tag.equals(string));
    }

    /**
     * Test to verify {@link Tag#hashCode()}
     */
    @Test
    public void testHashCode() {
        Tag tag = new Tag("Ads");
        Assert.assertTrue(tag.hashCode() == tag.getId().hashCode());
    }
}
