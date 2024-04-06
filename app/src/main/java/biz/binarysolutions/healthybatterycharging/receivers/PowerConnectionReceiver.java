package biz.binarysolutions.healthybatterycharging.receivers;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import biz.binarysolutions.healthybatterycharging.util.PreferencesUtil;

/**
 * 
 *
 */
public class PowerConnectionReceiver extends BroadcastReceiver {

    /**
     * Keep them in sync with actions from AndroidManifest.xml
     */
    private final static String[] ACTIONS = {
        "android.intent.action.ACTION_POWER_CONNECTED",
        "android.intent.action.ACTION_POWER_DISCONNECTED",
    };

    private boolean isValidAction(Intent intent) {

        if (intent == null) {
            return false;
        }

        String action = intent.getAction();
        if (action == null) {
            return false;
        }

        for (String validAction : ACTIONS) {
            if (action.equals(validAction)) {
                return true;
            }
        }

        return false;
    }

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {

        System.out.println("HBC ===> PowerConnectionReceiver.onReceive called");

        if (!isValidAction(intent)) {
            return;
        }

        AlarmReceiver.cancelNotification(context);

        int batteryLow  = PreferencesUtil.getBatteryLow(context);
        int batteryHigh = PreferencesUtil.getBatteryHigh(context);

    	AlarmReceiver.start(context, batteryLow, batteryHigh);
    }
}
