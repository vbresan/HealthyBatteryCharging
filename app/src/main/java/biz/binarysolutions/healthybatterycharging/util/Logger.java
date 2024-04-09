package biz.binarysolutions.healthybatterycharging.util;

import android.util.Log;

import biz.binarysolutions.healthybatterycharging.BuildConfig;

/**
 *
 */
public class Logger {

    /**
     *
     * @param tag
     * @param message
     */
    public static void d(String tag, String message) {

        if (BuildConfig.DEBUG) {
            Log.d(tag, message);
        }
    }
}
