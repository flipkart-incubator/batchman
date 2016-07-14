/*
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2016 Flipkart Internet Pvt. Ltd.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
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
