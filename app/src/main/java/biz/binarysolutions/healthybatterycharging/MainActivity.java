package biz.binarysolutions.healthybatterycharging;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.TextView;

/**
 * 
 *
 */
public class MainActivity extends Activity {
	
	private BroadcastReceiver receiver;

	/**
	 * 
	 */
	public void refresh() {
		
		IntentFilter filter  = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = registerReceiver(null, filter);
		
		boolean isCharging = BatteryUtil.isCharging(batteryStatus); 
		
		TextView textViewStatus = (TextView) findViewById(R.id.textViewBatteryStatus);
		String text = getString(isCharging? R.string.Charging : R.string.NotCharging);
		textViewStatus.setText(text);
		
		int batteryLevel = BatteryUtil.getBatteryLevel(batteryStatus);
		
		TextView textViewLevel = (TextView) findViewById(R.id.textViewBatteryLevel);
		textViewLevel.setText("" + batteryLevel + "%");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
		filter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");

		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				refresh();
			}
		};
		registerReceiver(receiver, filter);
		
		AlarmReceiver.start(this);
	}
	
	

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		refresh();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		
		if (receiver != null) {
			unregisterReceiver(receiver);
			receiver = null;
		}
		super.onDestroy();
	}
}
