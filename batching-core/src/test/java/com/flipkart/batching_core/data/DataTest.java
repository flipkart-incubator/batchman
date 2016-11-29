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

package com.flipkart.batching.core.data;

import com.flipkart.batching.core.Data;

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
