package biz.binarysolutions.healthybatterycharging;

import static android.app.AlarmManager.INTERVAL_FIFTEEN_MINUTES;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;

import biz.binarysolutions.healthybatterycharging.util.BatteryUtil;

/**
 * 
 *
 */
public class AlarmReceiver extends BroadcastReceiver {
	
	private static final int  ALARM_TYPE = AlarmManager.ELAPSED_REALTIME_WAKEUP;

	private static AlarmManager  alarmManager  = null;
	private static PendingIntent pendingIntent = null;
	
	private static NotificationManager notificationManager = null;

	private static int batteryLow;
	private static int batteryHigh;

	/**
	 *
	 * @param flags
	 */
	private static int getPendingIntentFlags(int flags) {

		if (Build.VERSION.SDK_INT >= 23) {
			flags |= PendingIntent.FLAG_IMMUTABLE;
		}

		return flags;
	}

	/**
	 *
	 * @param context
	 * @return
	 */
	@SuppressWarnings("UnnecessaryLocalVariable")
	private PendingIntent getDummyIntent(Context context) {

		PendingIntent intent = PendingIntent.getActivity(
			context,
			0,
			new Intent(),
			getPendingIntentFlags(0)
		);

		return intent;
	}

	/**
	 *
	 * @return
	 */
	private Uri getRingtone() {
		return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
	}
	
	/**
	 * 
	 * @param text
	 * @param color
	 * @param context
	 */
	private void displayNotification(String text, int color, Context context) {

		System.out.println("HBC ===> AlarmReceiver.displayNotification called");
		
		String title          = context.getString(R.string.app_name);
		long[] vibratePattern = new long[] { 0, 500, 500, 500, 500 };
		int    lightOn        = 1000;
		int    lightOff       = 300;
		
		PendingIntent dummyIntent = getDummyIntent(context);
		Uri           ringtone    = getRingtone();

		Notification.Builder builder = new Notification.Builder(context)
		    .setContentTitle(title)
		    .setContentText(text)
		    .setVibrate(vibratePattern)
		    .setLights(color, lightOn, lightOff)
		    .setSmallIcon(android.R.drawable.ic_notification_clear_all)
		    .setAutoCancel(true)
		    .setContentIntent(dummyIntent)
		    .setSound(ringtone);

		Notification notification;
		if (android.os.Build.VERSION.SDK_INT >= 16) {
			notification = builder.build();
		} else {
			notification = builder.getNotification();
		}

		if (notificationManager == null) {
			notificationManager = (NotificationManager) 
				context.getSystemService(Context.NOTIFICATION_SERVICE);
		}

		notificationManager.notify(0, notification);
	}

	/**
	 * @param context 
	 * 
	 */
	private void displayDisconnectChargerNotification(Context context) {

		System.out.println("HBC ===> AlarmReceiver.displayDisconnectChargerNotification called");
		
		String message = context.getString(R.string.DisconnectCharger);
		displayNotification(message, Color.GREEN, context);
	}

	/**
	 * @param context 
	 * 
	 */
	private void displayConnectChargerNotification(Context context) {

		System.out.println("HBC ===> AlarmReceiver.displayConnectChargerNotification called");
		
		String message = context.getString(R.string.ConnectCharger);
		displayNotification(message, Color.RED, context);
	}

	/**
	 * 
	 */
	private void stop() {

		System.out.println("HBC ===> AlarmReceiver.stop called");

		if (alarmManager != null && pendingIntent != null) {
			alarmManager.cancel(pendingIntent);
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {

		System.out.println("HBC ===> AlarmReceiver.onReceive called");
		
		Intent batteryStatus = BatteryUtil.getBatteryStatus(context);
		if (batteryStatus == null) {
			return;
		}
		
		boolean isCharging = BatteryUtil.isCharging(batteryStatus);
		int     level      = BatteryUtil.getBatteryLevel(batteryStatus);

		if (level >= batteryHigh && isCharging) {
			displayDisconnectChargerNotification(context);
			stop();
		} else if (level <= batteryLow && !isCharging) {
			displayConnectChargerNotification(context);
			stop();
		}
	}

	/**
	 * @param context
	 * @param batteryLow
	 * @param batteryHigh
	 */
	public static void start(Context context, int batteryLow, int batteryHigh) {

		System.out.println("HBC ===> AlarmReceiver.start called");

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
				getPendingIntentFlags(PendingIntent.FLAG_UPDATE_CURRENT)
			);
		}
		
		long now = SystemClock.elapsedRealtime();
	
		alarmManager.cancel(pendingIntent);
		alarmManager.setInexactRepeating(
			ALARM_TYPE, now, INTERVAL_FIFTEEN_MINUTES, pendingIntent
		);
	}

	/**
	 * 
	 */
	public static void cancelNotification() {

		System.out.println("HBC ===> AlarmReceiver.cancelNotification called");

		if (notificationManager != null) {
			notificationManager.cancelAll();
		}
	}
}
