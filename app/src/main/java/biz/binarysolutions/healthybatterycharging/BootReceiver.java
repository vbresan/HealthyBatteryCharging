package biz.binarysolutions.healthybatterycharging;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 
 *
 */
public class BootReceiver extends BroadcastReceiver {

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

		System.out.println("HBC ===> action: " + action);

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

		System.out.println("HBC ===> BootReceiver.onReceive called");

		if (!isValidAction(intent)) {
			return;
		}

		int batteryLow  = MainActivity.getBatteryLow(context);
		int batteryHigh = MainActivity.getBatteryHigh(context);

		AlarmReceiver.start(context, batteryLow, batteryHigh);
	}
}
