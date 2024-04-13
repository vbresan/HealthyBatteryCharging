package biz.binarysolutions.healthybatterycharging.receivers;

import static biz.binarysolutions.healthybatterycharging.MainActivity.DEFAULT_BATTERY_HIGH;
import static biz.binarysolutions.healthybatterycharging.MainActivity.DEFAULT_BATTERY_LOW;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import biz.binarysolutions.healthybatterycharging.BuildConfig;
import biz.binarysolutions.healthybatterycharging.util.Battery;
import biz.binarysolutions.healthybatterycharging.util.IntentUtil;
import biz.binarysolutions.healthybatterycharging.util.Logger;

/**
 * 
 *
 */
public class AlarmReceiver extends BroadcastReceiver {

	private static final String TAG = AlarmReceiver.class.getSimpleName();

	private static final String PACKAGE_NAME = BuildConfig.APPLICATION_ID;
	private static final String INTENT_EXTRA_LOW =
		PACKAGE_NAME + ".intentExtra.low";
	private static final String INTENT_EXTRA_HIGH =
		PACKAGE_NAME + ".intentExtra.high";

	private static final int  ALARM_TYPE = AlarmManager.ELAPSED_REALTIME_WAKEUP;
	private static final long INTERVAL   = AlarmManager.INTERVAL_FIFTEEN_MINUTES;

	/**
	 * @param context
	 * @param batteryLow
	 * @param batteryHigh
	 */
	private static void checkConditions
		(
			Context context,
			int 	batteryLow,
			int 	batteryHigh
		) {

		Intent batteryStatus = Battery.getBatteryStatus(context);
		if (batteryStatus == null) {
			return;
		}

		boolean isCharging = Battery.isCharging(batteryStatus);
		int     level      = Battery.getBatteryLevel(batteryStatus);

		Logger.d(TAG, "is charging      : " + isCharging);
		Logger.d(TAG, "battery level    : " + level);
		Logger.d(TAG, "notification low : " + batteryLow);
		Logger.d(TAG, "notification high: " + batteryHigh);

		if (level >= batteryHigh && isCharging) {
			Notifications.displayDisconnectChargerNotification(context);
		} else if (level <= batteryLow && !isCharging) {
			Notifications.displayConnectChargerNotification(context);
		} else {
			Notifications.cancellAll(context);
		}
	}

	/**
	 * @param context
	 * @param batteryLow
	 * @param batteryHigh
	 * @return
	 */
	private static Intent getIntent
		(
			Context context,
			int 	batteryLow,
			int 	batteryHigh
		) {

		Intent intent = new Intent(context, AlarmReceiver.class);
		intent.putExtra(INTENT_EXTRA_LOW,  batteryLow);
		intent.putExtra(INTENT_EXTRA_HIGH, batteryHigh);

		return intent;
	}

	/**
	 * @param context
	 * @param batteryLow
	 * @param batteryHigh
	 */
	private static void scheduleAlarms
		(
			Context context,
			int 	batteryLow,
			int 	batteryHigh
		) {

		AlarmManager alarmManager = (AlarmManager)
			context.getSystemService(Context.ALARM_SERVICE);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(
			context,
			0,
			getIntent(context, batteryLow, batteryHigh),
			IntentUtil.getPendingIntentFlags(PendingIntent.FLAG_UPDATE_CURRENT)
		);

		long firstTrigger = SystemClock.elapsedRealtime() + INTERVAL;
		alarmManager.setInexactRepeating(
			ALARM_TYPE, firstTrigger, INTERVAL, pendingIntent
		);
	}

	@Override
	public void onReceive(Context context, Intent intent) {

		Logger.d(TAG, "onReceive called");

		if (intent == null) {
			return;
		}

		int batteryLow  = intent.getIntExtra(INTENT_EXTRA_LOW,  DEFAULT_BATTERY_LOW);
		int batteryHigh = intent.getIntExtra(INTENT_EXTRA_HIGH, DEFAULT_BATTERY_HIGH);

		Context applicationContext = context.getApplicationContext();
		checkConditions(applicationContext, batteryLow, batteryHigh);
	}

	/**
	 * @param context
	 * @param batteryLow
	 * @param batteryHigh
	 */
	public static void start(Context context, int batteryLow, int batteryHigh) {

		Logger.d(TAG, "start called [" + batteryLow + ", " + batteryHigh + "]");

		Context applicationContext = context.getApplicationContext();
		checkConditions(applicationContext, batteryLow, batteryHigh);
		scheduleAlarms(applicationContext, batteryLow, batteryHigh);
	}
}
