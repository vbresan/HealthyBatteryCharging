package biz.binarysolutions.healthybatterycharging.receivers;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import biz.binarysolutions.healthybatterycharging.util.Logger;
import biz.binarysolutions.healthybatterycharging.util.PreferencesUtil;

/**
 * 
 *
 */
public class BootReceiver extends BroadcastReceiver {

	private static final String TAG = BootReceiver.class.getSimpleName();

	/**
	 * Keep them in sync with actions from AndroidManifest.xml
	 */
	private final static String[] ACTIONS = {
		"android.intent.action.BOOT_COMPLETED",
        "android.intent.action.QUICKBOOT_POWERON",
        "com.htc.intent.action.QUICKBOOT_POWERON",
	};

	private boolean isValidAction(Intent intent) {

		if (intent == null) {
			return false;
		}

		String action = intent.getAction();
		if (action == null) {
			return false;
		}

		Logger.d(TAG, "action: " + action);

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

		Logger.d(TAG, "onReceive called");

		if (!isValidAction(intent)) {
			return;
		}

		int batteryLow  = PreferencesUtil.getBatteryLow(context);
		int batteryHigh = PreferencesUtil.getBatteryHigh(context);

		AlarmReceiver.start(context, batteryLow, batteryHigh);
	}
}
