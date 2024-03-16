package biz.binarysolutions.healthybatterycharging;

import android.content.Intent;
import android.os.BatteryManager;

/**
 * 
 *
 */
public class BatteryUtil {
	
	/**
	 * 
	 * @param batteryStatus
	 * @return
	 */
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
	public static int getBatteryLevel(Intent batteryStatus) {
	
		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
	
		int batteryLevel = (int) ((level / (float) scale) * 100);
		
		return batteryLevel;
	}	
}
