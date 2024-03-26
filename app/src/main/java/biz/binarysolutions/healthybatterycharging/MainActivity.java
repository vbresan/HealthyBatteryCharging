package biz.binarysolutions.healthybatterycharging;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.preference.PreferenceManager;

import java.util.Locale;

import biz.binarysolutions.healthybatterycharging.util.BatteryUtil;
import biz.binarysolutions.healthybatterycharging.util.DefaultTextWatcher;

/**
 * 
 *
 */
public class MainActivity extends Activity {
	
	private final Locale locale = Locale.getDefault();

	private static final int DEFAULT_BATTERY_LOW  = 40;
	private static final int DEFAULT_BATTERY_HIGH = 80;

	private BroadcastReceiver receiver;

	private int batteryLow;
	private int batteryHigh;

	private void setEditText(EditText editText, int value) {

		if (editText == null) {
			return;
		}

		editText.setText(String.format(locale, "%d", value));
	}

	private void loadThresholds() {

		SharedPreferences preferences =
			PreferenceManager.getDefaultSharedPreferences(this);

		batteryLow  = preferences.getInt("batteryLow",  DEFAULT_BATTERY_LOW);
		batteryHigh = preferences.getInt("batteryHigh", DEFAULT_BATTERY_HIGH);

		EditText editTextLow = findViewById(R.id.editTextLow);
		setEditText(editTextLow, batteryLow);

		EditText editTextHigh = findViewById(R.id.editTextHigh);
		setEditText(editTextHigh, batteryHigh);
	}

	private void saveThresholds() {

		EditText editTextLow  = findViewById(R.id.editTextLow);
		EditText editTextHigh = findViewById(R.id.editTextHigh);

		if (editTextLow == null || editTextHigh == null) {
			return;
		}

		try {
			batteryLow  = Integer.parseInt(editTextLow.getText().toString());
			batteryHigh = Integer.parseInt(editTextHigh.getText().toString());
		} catch (NumberFormatException e) {
			// do nothing, this should not happen as it has been checked already
		}

		SharedPreferences preferences =
			PreferenceManager.getDefaultSharedPreferences(this);

		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt("batteryLow",  batteryLow);
		editor.putInt("batteryHigh", batteryHigh);
		editor.apply();

		setButtonSaveEnabled(false);
		AlarmReceiver.start(this, batteryLow, batteryHigh);
	}

	private void resetThresholds() {

		batteryLow  = DEFAULT_BATTERY_LOW;
		batteryHigh = DEFAULT_BATTERY_HIGH;

		EditText editTextLow = findViewById(R.id.editTextLow);
		setEditText(editTextLow, batteryLow);

		EditText editTextHigh = findViewById(R.id.editTextHigh);
		setEditText(editTextHigh, batteryHigh);

		saveThresholds();
	}

	private void setButtonEnabled(int id, boolean isEnabled) {

		Button button = findViewById(id);
		if (button != null) {
			button.setEnabled(isEnabled);
		}
	}

	private void setButtonSaveEnabled(boolean isEnabled) {
		setButtonEnabled(R.id.buttonSave, isEnabled);
	}

	private void setButtonResetEnabled(boolean isEnabled) {
		setButtonEnabled(R.id.buttonReset, isEnabled);
	}

	private void addEditTextListeners() {

		EditText editTextLow  = findViewById(R.id.editTextLow);
		EditText editTextHigh = findViewById(R.id.editTextHigh);

		if (editTextLow == null || editTextHigh == null) {
			return;
		}

		DefaultTextWatcher textWatcher = new DefaultTextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {

				int low;
				int high;

				try {
					low  = Integer.parseInt(editTextLow.getText().toString());
					high = Integer.parseInt(editTextHigh.getText().toString());
				} catch (NumberFormatException e) {

					setButtonSaveEnabled(false);
					setButtonResetEnabled(true);

					return;
				}

				setButtonSaveEnabled((low != batteryLow || high != batteryHigh) && low < high);
				setButtonResetEnabled(low != DEFAULT_BATTERY_LOW || high != DEFAULT_BATTERY_HIGH);
			}
		};

		editTextLow.addTextChangedListener(textWatcher);
		editTextHigh.addTextChangedListener(textWatcher);
	}

	private void addListeners() {

		Button buttonSave = findViewById(R.id.buttonSave);
		if (buttonSave != null) {
			buttonSave.setOnClickListener(v -> saveThresholds());
		}

		Button buttonReset = findViewById(R.id.buttonReset);
		if (buttonReset != null) {
			buttonReset.setOnClickListener(v -> resetThresholds());
		}

		addEditTextListeners();
	}

	/**
	 * 
	 */
	private void refresh() {

		Intent batteryStatus = BatteryUtil.getBatteryStatus(this);
		if (batteryStatus == null) {
			return;
		}

		TextView textViewStatus = findViewById(R.id.textViewBatteryStatus);
		if (textViewStatus != null) {

			boolean isCharging = BatteryUtil.isCharging(batteryStatus);
			String  text       = getString(isCharging? R.string.Charging : R.string.Discharging);
			textViewStatus.setText(text);
		}

		TextView textViewLevel = findViewById(R.id.textViewBatteryLevel);
		if (textViewLevel != null) {

			int batteryLevel = BatteryUtil.getBatteryLevel(batteryStatus);
			textViewLevel.setText(String.format(locale, "%d%%", batteryLevel));
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		loadThresholds();
		addListeners();
		
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

		AlarmReceiver.start(this, batteryLow, batteryHigh);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		refresh();
	}

	@Override
	protected void onDestroy() {
		
		if (receiver != null) {
			unregisterReceiver(receiver);
			receiver = null;
		}
		super.onDestroy();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {

		if (event.getAction() == MotionEvent.ACTION_DOWN) {

			View v = getCurrentFocus();
			if (v instanceof EditText) {

				Rect outRect = new Rect();
				v.getGlobalVisibleRect(outRect);
				if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
					v.clearFocus();
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				}
			}
		}

		return super.dispatchTouchEvent( event );
	}

	public static int getBatteryLow(Context context) {

		SharedPreferences preferences =
			PreferenceManager.getDefaultSharedPreferences(context);

		return preferences.getInt("batteryLow", DEFAULT_BATTERY_LOW);
	}

	public static int getBatteryHigh(Context context) {

		SharedPreferences preferences =
			PreferenceManager.getDefaultSharedPreferences(context);

		return preferences.getInt("batteryHigh", DEFAULT_BATTERY_HIGH);
	}
}
