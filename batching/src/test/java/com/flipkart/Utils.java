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

package com.flipkart;

import com.flipkart.batching.Data;
import com.flipkart.batching.data.EventData;
import com.flipkart.batching.data.Tag;
import com.flipkart.batching.data.TagData;

import java.util.ArrayList;

/**
 * Utils class for Test
 */
public class Utils {

    /**
     * Method to create fake array list of Data.
     *
     * @param size
     * @return dataList
     */
    static Data eventData;

    public static ArrayList<Data> fakeCollection(int size) {
        ArrayList<Data> dataList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            eventData = new EventData();
            eventData.setEventId(System.currentTimeMillis() + System.nanoTime() + i);
            dataList.add(eventData);
        }
        return dataList;
    }

    public static ArrayList<Data> fakeAdsCollection(int size) {
        ArrayList<Data> dataList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            eventData = new EventData();
            eventData.setEventId(System.currentTimeMillis() + System.nanoTime() + i);
            dataList.add(eventData);
        }
        return dataList;
    }

    public static ArrayList<Data> fakeDebugCollection(int size) {
        ArrayList<Data> dataList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            eventData = new EventData();
            eventData.setEventId(System.currentTimeMillis() + System.nanoTime() + i);
            dataList.add(eventData);
        }
        return dataList;
    }

    public static ArrayList<Data> fakeBuisnessCollection(int size) {
        ArrayList<Data> dataList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            eventData = new EventData();
            eventData.setEventId(System.currentTimeMillis() + System.nanoTime() + i);
            dataList.add(eventData);
        }
        return dataList;
    }

    public static ArrayList<TagData> fakeTagAdsCollection(int size) {
        ArrayList<TagData> dataList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            TagData eventData = new TagData(new Tag("ADS"));
            eventData.setEventId(System.currentTimeMillis() + System.nanoTime() + i);
            dataList.add(eventData);
        }
        return dataList;
    }

    public static ArrayList<TagData> fakeTagDebugCollection(int size) {
        ArrayList<TagData> dataList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            TagData eventData = new TagData(new Tag("DEBUG"));
            eventData.setEventId(System.currentTimeMillis() + System.nanoTime() + i);
            dataList.add(eventData);
        }
        return dataList;
    }

}
