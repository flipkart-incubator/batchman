package com.flipkart.batchdemo;

import org.json.JSONObject;

/**
 * Created by kushal.sharma on 15/02/16.
 */
public class TrackingHelper {

    public static JSONObject getProductPageViewEvent(String listingId, String productId, String requestId) {
        try {
            if (requestId == null) requestId = "";
            JSONObject jsonEvent = new JSONObject();
            jsonEvent.put("event", "PRODUCT_VIEW");

            JSONObject dataJson = new JSONObject();
            dataJson.put("listingId", listingId);
            dataJson.put("productId", productId);
            dataJson.put("requestId", requestId);
            dataJson.put("timestamp", System.currentTimeMillis());

            jsonEvent.put("data", dataJson);
            return jsonEvent;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
