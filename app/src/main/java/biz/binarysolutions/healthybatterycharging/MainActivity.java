package biz.binarysolutions.healthybatterycharging;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.preference.PreferenceManager;

import org.jetbrains.annotations.NotNull;

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

		if (editText != null) {
			editText.setText(String.format(locale, "%d", value));
		}
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

				try {
					int low  = Integer.parseInt(editTextLow.getText().toString());
					int high = Integer.parseInt(editTextHigh.getText().toString());

					boolean isModified = low != batteryLow || high != batteryHigh;
					setButtonSaveEnabled(isModified && low < high);

					boolean isDefault  = low == DEFAULT_BATTERY_LOW && high == DEFAULT_BATTERY_HIGH;
					setButtonResetEnabled(!isDefault);

				} catch (NumberFormatException e) {

					setButtonSaveEnabled(false);
					setButtonResetEnabled(true);
				}
			}
		};

		editTextLow.addTextChangedListener(textWatcher);
		editTextHigh.addTextChangedListener(textWatcher);
	}

	private void addListeners() {

		Button buttonSave = findViewById(R.id.buttonSave);
		if (buttonSave != null) {
			buttonSave.setOnClickListener(v -> saveThresholds());
			/*
			buttonSave.setOnTouchListener(() -> {
				System.out.println("Touch me baby!");
			});

			 */
		}

		Button buttonReset = findViewById(R.id.buttonReset);
		if (buttonReset != null) {
			buttonReset.setOnClickListener(v -> resetThresholds());
		}

		addEditTextListeners();
	}

	private int getVerticalDelta
		(
			@NotNull RelativeLayout container,
			@NotNull Button 		button,
			@NotNull ImageView 	    imageView
		) {

		Rect buttonRect = new Rect();
		button.getDrawingRect(buttonRect);
		container.offsetDescendantRectToMyCoords(button, buttonRect);

		Rect imageRect  = new Rect();
		imageView.getDrawingRect(imageRect);
		container.offsetDescendantRectToMyCoords(imageView, imageRect);

		return buttonRect.centerY() - imageRect.centerY();
	}

	private void moveImageVertically(@NotNull ImageView imageView, int delta) {

		RelativeLayout.LayoutParams params =
			(RelativeLayout.LayoutParams) imageView.getLayoutParams();

		params.setMargins(
			params.leftMargin,
			params.topMargin + delta,
			params.rightMargin,
			params.bottomMargin
		);

		imageView.setLayoutParams(params);
	}

	private void positionGlowImage
		(
			@NotNull RelativeLayout container,
			int buttonId,
			int imageViewId
		) {

		Button    button    = findViewById(buttonId);
		ImageView imageView = findViewById(imageViewId);

		if (button == null || imageView == null) {
			return;
		}

		int delta = getVerticalDelta(container, button, imageView);
		moveImageVertically(imageView, delta);

		System.out.println("====================> delta: " + delta);
	}

	private void positionGlowImages() {

		RelativeLayout container = findViewById(R.id.relativeLayoutContainer);
		if (container == null) {
			return;
		}

		container.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {

				if (Build.VERSION.SDK_INT >= 16) {
					container.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				} else {
					container.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				}

				positionGlowImage(container, R.id.buttonSave,  R.id.imageViewSave);
				positionGlowImage(container, R.id.buttonReset, R.id.imageViewReset);
			}
		});
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
		positionGlowImages();
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
