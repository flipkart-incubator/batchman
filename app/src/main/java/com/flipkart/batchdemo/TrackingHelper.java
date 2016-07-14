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

package com.flipkart.batchdemo;

import org.json.JSONArray;
import org.json.JSONObject;

public class TrackingHelper {

    public static JSONObject getProductPageViewEvent(String listingId, String productId, String requestId) {
        try {
            if (requestId == null) requestId = "";
            JSONObject jsonEvent = new JSONObject();
            jsonEvent.put("event", "PRODUCT_VIEW");

            JSONArray jsonArray = new JSONArray();
            for(int idx = 0 ; idx < 5; idx++)
            {
                JSONObject dataJson = new JSONObject();
                dataJson.put("listingId", listingId);
                dataJson.put("productId", productId);
                dataJson.put("requestId", requestId);
                dataJson.put("timestamp", System.currentTimeMillis());
                jsonArray.put(dataJson);
            }


            jsonEvent.put("data", jsonArray);
            return jsonEvent;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
