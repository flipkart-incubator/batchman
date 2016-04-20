package com.flipkart;

import com.flipkart.batching.Data;
import com.flipkart.batching.data.EventData;
import com.flipkart.batching.data.Tag;
import com.flipkart.batching.data.TagData;

import java.util.ArrayList;

/**
 * Created by anirudh.r on 12/02/16.
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
