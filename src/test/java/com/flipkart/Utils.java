package com.flipkart;

import com.flipkart.data.Data;
import com.flipkart.data.EventData;
import com.flipkart.data.Tag;

import java.util.ArrayList;

/**
 * Created by anirudh.r on 12/02/16.
 */
public class Utils {

    /**
     * Method to create fake array list of Data.
     *
     * @param size
     * @return
     */
    static Data eventData;

    public static ArrayList<Data> fakeCollection(int size) {
        ArrayList<Data> datas = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            eventData = new EventData(new Tag("u" + i), "Event " + i);
            datas.add(eventData);
        }
        return datas;
    }

    public static ArrayList<Data> fakeAdsCollection(int size) {
        ArrayList<Data> datas = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            eventData = new EventData(new Tag("AD"), "Event " + i);
            datas.add(eventData);
        }
        return datas;
    }
}
