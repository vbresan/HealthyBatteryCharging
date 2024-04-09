package biz.binarysolutions.healthybatterycharging.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import biz.binarysolutions.healthybatterycharging.util.Battery;
import biz.binarysolutions.healthybatterycharging.util.IntentUtil;
import biz.binarysolutions.healthybatterycharging.util.Notifications;

/**
 * 
 *
 */
public class AlarmReceiver extends BroadcastReceiver {

	private static final int  ALARM_TYPE = AlarmManager.ELAPSED_REALTIME_WAKEUP;
	private static final long INTERVAL   = AlarmManager.INTERVAL_FIFTEEN_MINUTES / 5;

	private static AlarmManager  alarmManager  = null;
	private static PendingIntent pendingIntent = null;
	
	private static int batteryLow;
	private static int batteryHigh;

	@Override
	public void onReceive(Context context, Intent intent) {

		System.out.println("HBC ===> AlarmReceiver.onReceive called");

		Intent batteryStatus = Battery.getBatteryStatus(context);
		if (batteryStatus == null) {
			return;
		}
		
		boolean isCharging = Battery.isCharging(batteryStatus);
		int     level      = Battery.getBatteryLevel(batteryStatus);

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
	 */
	public static void start(Context context, int batteryLow, int batteryHigh) {

		System.out.println("HBC ===> AlarmReceiver.start called (" + batteryLow + ", " + batteryHigh + ")");

		AlarmReceiver.batteryLow  = batteryLow;
		AlarmReceiver.batteryHigh = batteryHigh;
		
		if (alarmManager == null) {
			alarmManager = (AlarmManager) 
				context.getSystemService(Context.ALARM_SERVICE);
		}
		
		if (pendingIntent == null) {
			pendingIntent = PendingIntent.getBroadcast(
				context, 
				0, 
				new Intent(context, AlarmReceiver.class),
				IntentUtil.getPendingIntentFlags(PendingIntent.FLAG_UPDATE_CURRENT)
			);
		}
		
		long firstTrigger = SystemClock.elapsedRealtime() - INTERVAL;
		alarmManager.setInexactRepeating(
			ALARM_TYPE, firstTrigger, INTERVAL, pendingIntent
		);
	}
}
