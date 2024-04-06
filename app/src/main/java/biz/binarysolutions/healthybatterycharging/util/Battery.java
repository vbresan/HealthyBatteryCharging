package biz.binarysolutions.healthybatterycharging.util;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

/**
 * 
 *
 */
public class Battery {
	
	/**
	 * 
	 * @param batteryStatus
	 * @return
	 */
	@SuppressWarnings("UnnecessaryLocalVariable")
	public static boolean isCharging(Intent batteryStatus) {
		
		int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		
		boolean isCharging = 
				status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

		return isCharging;
	}	

	/**
	 * 
	 * @param batteryStatus
	 * @return
	 */
	@SuppressWarnings("UnnecessaryLocalVariable")
	public static int getBatteryLevel(Intent batteryStatus) {
	
		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
	
		int batteryLevel = (int) ((level / (float) scale) * 100);
		
		return batteryLevel;
	}

	/**
	 *
	 * @param context
	 * @return
	 */
	public static Intent getBatteryStatus(Context context) {

		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		return context.getApplicationContext().registerReceiver(null, filter);
	}
}
