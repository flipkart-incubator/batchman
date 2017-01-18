package com.flipkart.batching.toolbox;

import android.util.Log;

public class LogUtil {
    public static boolean isLoggingEnabled = false;

    public static void log(String tag, String message) {
        if (isLoggingEnabled) {
            Log.d(tag, message);
        }
    }
}