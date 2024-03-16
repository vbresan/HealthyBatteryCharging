package biz.binarysolutions.healthybatterycharging;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 
 *
 */
public class PowerConnectionReceiver extends BroadcastReceiver {
	
    @Override
    public void onReceive(Context context, Intent intent) {

    	AlarmReceiver.cancelNotification();
    	AlarmReceiver.start(context);
    	
		//TODO: trigeri na uključen/isključen charger
    }
}
