package biz.binarysolutions.healthybatterycharging.util;

import static biz.binarysolutions.healthybatterycharging.MainActivity.DEFAULT_BATTERY_HIGH;
import static biz.binarysolutions.healthybatterycharging.MainActivity.DEFAULT_BATTERY_LOW;
import static biz.binarysolutions.healthybatterycharging.MainActivity.DEFAULT_INTERVAL;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

/**
 *
 */
public class PreferencesUtil {

    /**
     *
     * @param context
     * @return
     */
    public static int getBatteryLow(Context context) {

        SharedPreferences preferences =
            PreferenceManager.getDefaultSharedPreferences(context);

        return preferences.getInt("batteryLow", DEFAULT_BATTERY_LOW);
    }

    /**
     *
     * @param context
     * @return
     */
    public static int getBatteryHigh(Context context) {

        SharedPreferences preferences =
            PreferenceManager.getDefaultSharedPreferences(context);

        return preferences.getInt("batteryHigh", DEFAULT_BATTERY_HIGH);
    }

    public static int getInterval(Context context) {

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(context);

        return preferences.getInt("interval", DEFAULT_INTERVAL);
    }
}
