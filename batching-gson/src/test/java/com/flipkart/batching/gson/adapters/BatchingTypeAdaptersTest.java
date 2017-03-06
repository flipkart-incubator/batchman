package com.flipkart.batching.gson.adapters;

import com.flipkart.batching.gson.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class BatchingTypeAdaptersTest {

    @Test
    public void testJSONArrayTypeAdapter() throws Exception {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(true);
        jsonArray.put(1);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key", "value");
        jsonObject.put("key1", "value1");
        jsonObject.put("key2", "value2");

        jsonArray.put(jsonObject);

        Gson gson = new Gson();

        TypeAdapter<JSONArray> jsonArrayTypeAdapter = BatchingTypeAdapters.getJSONArrayTypeAdapter(gson);
        String toJson = jsonArrayTypeAdapter.toJson(jsonArray);
        JSONArray jsonArray1 = jsonArrayTypeAdapter.fromJson(toJson);

        Assert.assertTrue(jsonArray.toString().equals(jsonArray1.toString()));
    }

    @Test
    public void testJSONObjectTypeAdapter() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key", "value");
        jsonObject.put("key1", "value1");
        jsonObject.put("key2", "value2");

        Gson gson = new Gson();

        TypeAdapter<JSONObject> jsonObjectTypeAdapter = BatchingTypeAdapters.getJSONObjectTypeAdapter(gson);
        String toJson = jsonObjectTypeAdapter.toJson(jsonObject);
        JSONObject jsonObject1 = jsonObjectTypeAdapter.fromJson(toJson);

        Assert.assertTrue(jsonObject1.toString().equals(jsonObject.toString()));
    }
}