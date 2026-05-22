package biz.binarysolutions.healthybatterycharging;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

import biz.binarysolutions.healthybatterycharging.databinding.ActivityMainBinding;
import biz.binarysolutions.healthybatterycharging.receivers.AlarmReceiver;
import biz.binarysolutions.healthybatterycharging.util.Battery;
import biz.binarysolutions.healthybatterycharging.util.DefaultTextWatcher;
import biz.binarysolutions.healthybatterycharging.util.Logger;
import biz.binarysolutions.healthybatterycharging.util.NotificationChannelUtil;

public class MainActivity extends Activity {

	private ActivityMainBinding binding;

	private static final String TAG = MainActivity.class.getSimpleName();

	private final Locale locale = Locale.getDefault();

	private static final String PERMISSION_POST_NOTIFICATION =
		"android.permission.POST_NOTIFICATIONS";

	private static final double GLOW_SCALE_WIDTH  = 1.35;
	private static final double GLOW_SCALE_HEIGHT = 2.4;

	public static final int DEFAULT_BATTERY_LOW  = 40;
	public static final int DEFAULT_BATTERY_HIGH = 80;
	public static final int DEFAULT_INTERVAL = 15;

	private BroadcastReceiver receiver;

	private int batteryLow;
	private int batteryHigh;
	private int interval;

	private void setEditText(EditText editText, int value) {

		if (editText != null) {
			editText.setText(String.format(locale, "%d", value));
		}
	}

	/**
	 *
	 * @param editText edit text to get value from
	 * @return int value
	 * @throws NumberFormatException if value can not be converted to itn
	 * @throws NullPointerException if edit text is null
	 */
	private int getEditText(EditText editText) {
		return Integer.parseInt(editText.getText().toString());
	}

	private void loadPreferences() {

		SharedPreferences preferences =
			PreferenceManager.getDefaultSharedPreferences(this);

		batteryLow  = preferences.getInt("batteryLow",  DEFAULT_BATTERY_LOW);
		batteryHigh = preferences.getInt("batteryHigh", DEFAULT_BATTERY_HIGH);
		interval    = preferences.getInt("interval",    DEFAULT_INTERVAL);

		setEditText(binding.editTextLow.getRoot(),      batteryLow);
		setEditText(binding.editTextHigh.getRoot(),     batteryHigh);
		setEditText(binding.editTextInterval.getRoot(), interval);

		boolean isDefault =
			batteryLow  == DEFAULT_BATTERY_LOW &&
			batteryHigh == DEFAULT_BATTERY_HIGH &&
			interval    == DEFAULT_INTERVAL;
		setButtonResetEnabled(!isDefault);
	}

	private void savePreferences() {

		try {
			batteryLow  = getEditText(binding.editTextLow.getRoot());
			batteryHigh = getEditText(binding.editTextHigh.getRoot());
			interval    = getEditText(binding.editTextInterval.getRoot());
		} catch (NumberFormatException | NullPointerException e) {
			// do nothing, this should not happen as it has been checked already
		}

		SharedPreferences preferences =
			PreferenceManager.getDefaultSharedPreferences(this);

		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt("batteryLow",  batteryLow);
		editor.putInt("batteryHigh", batteryHigh);
		editor.putInt("interval",    interval);
		editor.apply();

		setButtonSaveEnabled(false);

		AlarmReceiver.start(this, batteryLow, batteryHigh, interval);
	}

	private void resetPreferences() {

		batteryLow  = DEFAULT_BATTERY_LOW;
		batteryHigh = DEFAULT_BATTERY_HIGH;
		interval    = DEFAULT_INTERVAL;

		setEditText(binding.editTextLow.getRoot(),      batteryLow);
		setEditText(binding.editTextHigh.getRoot(),     batteryHigh);
		setEditText(binding.editTextInterval.getRoot(), interval);

		savePreferences();
	}

	private void donate() {

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(getString(R.string.DonationURL)));
		startActivity(intent);
	}

	private void setButtonEnabled(Button button, boolean isEnabled) {
		button.setEnabled(isEnabled);
	}

	private void setButtonSaveEnabled(boolean isEnabled) {
		setButtonEnabled(binding.buttonSave, isEnabled);
	}

	private void setButtonResetEnabled(boolean isEnabled) {
		setButtonEnabled(binding.buttonReset, isEnabled);
	}

	@SuppressLint("ClickableViewAccessibility")
	private void addButtonListeners() {

		binding.buttonSave.setOnClickListener(v -> savePreferences());
		binding.buttonSave.setOnTouchListener((v, event) ->
			toggleGlow(event, binding.imageViewSave)
		);

		binding.buttonReset.setOnClickListener(v -> resetPreferences());
		binding.buttonReset.setOnTouchListener((v, event) ->
			toggleGlow(event, binding.imageViewReset)
		);

		binding.buttonDonate.setOnClickListener(v -> donate());
		binding.buttonDonate.setOnTouchListener((v, event) ->
			toggleGlow(event, binding.imageViewDonate)
		);
	}

	private void addEditTextListeners() {

		DefaultTextWatcher textWatcher = new DefaultTextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {

				try {
					int newLow      = getEditText(binding.editTextLow.getRoot());
					int newHigh     = getEditText(binding.editTextHigh.getRoot());
					int newInterval = getEditText(binding.editTextInterval.getRoot());

					boolean isModified =
						newLow      != batteryLow  ||
						newHigh     != batteryHigh ||
						newInterval != interval;
					boolean isValid =
						newLow      < newHigh &&
						newInterval > 0       &&
						newInterval <= DEFAULT_INTERVAL;
					setButtonSaveEnabled(isModified && isValid);

					boolean isDefault  =
						newLow      == DEFAULT_BATTERY_LOW  &&
						newHigh     == DEFAULT_BATTERY_HIGH &&
						newInterval == DEFAULT_INTERVAL;
					setButtonResetEnabled(!isDefault);

				} catch (NumberFormatException | NullPointerException e) {

					setButtonSaveEnabled(false);
					setButtonResetEnabled(true);
				}
			}
		};

		binding.editTextLow.getRoot().addTextChangedListener(textWatcher);
		binding.editTextHigh.getRoot().addTextChangedListener(textWatcher);
		binding.editTextInterval.getRoot().addTextChangedListener(textWatcher);
	}

	private boolean toggleGlow(MotionEvent event, ImageView imageView) {

		if (imageView != null) {

			int action = event.getAction();
			if (action == MotionEvent.ACTION_DOWN) {
				imageView.setVisibility(View.VISIBLE);
			} else if (action == MotionEvent.ACTION_UP) {
				imageView.setVisibility(View.INVISIBLE);
			} else if (action == MotionEvent.ACTION_CANCEL) {
				imageView.setVisibility(View.INVISIBLE);
			}
		}

		return false;
	}

	/**
	 *
	 */
	private void addListeners() {

		addButtonListeners();
		addEditTextListeners();
	}

	private void registerPowerConnectionReceiver() {

		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
		filter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");

		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {

				refreshBatteryStatus();
				AlarmReceiver.start(MainActivity.this, batteryLow, batteryHigh, interval);
			}
		};
		registerReceiver(receiver, filter);
	}

	/**
	 *
	 * @param container
	 * @param button
	 * @param imageView
	 * @return
	 */
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

	private void scaleImage
		(
			@NotNull ImageView imageView,
			@NotNull Button    button
		) {

		int width  = (int) (button.getWidth()  * GLOW_SCALE_WIDTH);
		int height = (int) (button.getHeight() * GLOW_SCALE_HEIGHT);

		RelativeLayout.LayoutParams params =
			(RelativeLayout.LayoutParams) imageView.getLayoutParams();

		params.width  = width;
		params.height = height;
	}

	private void positionGlowImage
		(
			@NotNull RelativeLayout container,
			Button    button,
			ImageView imageView
		) {

		scaleImage(imageView, button);

		int delta = getVerticalDelta(container, button, imageView);
		moveImageVertically(imageView, delta);
	}

	private void resizeDonateButton() {

		binding.buttonReset.post(() -> {

			ViewGroup.LayoutParams params = binding.buttonDonate.getLayoutParams();
			params.width = binding.buttonReset.getWidth();
			binding.buttonDonate.setLayoutParams(params);

			/* button and glow background do not share the same-width parent,
				so image has to be positioned (scaled) when button width is
				set.
			 */
			binding.buttonDonate.post(() ->
				positionGlowImage(
					binding.relativeLayoutDonate,
					binding.buttonDonate,
					binding.imageViewDonate
            	)
			);
		});
	}

	private void positionGlowImages() {

		RelativeLayout controls = binding.relativeLayoutControls;
		controls.post(() -> {
			positionGlowImage(controls, binding.buttonSave,  binding.imageViewSave);
			positionGlowImage(controls, binding.buttonReset, binding.imageViewReset);
		});
	}

	private void refreshBatteryStatus() {

		Intent batteryStatus = Battery.getBatteryStatus(this);
		if (batteryStatus == null) {
			return;
		}

		boolean isCharging = Battery.isCharging(batteryStatus);
		String  text       = getString(isCharging? R.string.Charging : R.string.Discharging);
		binding.textViewBatteryStatus.setText(text);

		int batteryLevel = Battery.getBatteryLevel(batteryStatus);
		binding.textViewBatteryLevel.setText(String.format(locale, "%d%%", batteryLevel));
	}

	/**
	 *
	 * @return
	 */
	private boolean hasNotificationPermission() {

		if (Build.VERSION.SDK_INT < 23) {
			return true;
		}

		int permission = checkSelfPermission(PERMISSION_POST_NOTIFICATION);
		return permission == PackageManager.PERMISSION_GRANTED;
	}

	/**
	 *
	 */
	private void createNotificationChannels() {

		if (Build.VERSION.SDK_INT >= 33 && !hasNotificationPermission()) {
			requestPermissions(new String[]{ PERMISSION_POST_NOTIFICATION }, 0);
		} else {
			NotificationChannelUtil.createChannels(this);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		createNotificationChannels();

		loadPreferences();
		addListeners();

		registerPowerConnectionReceiver();

		Logger.d(TAG, "onCreate: calling AlarmReceiver.start()");
		AlarmReceiver.start(this, batteryLow, batteryHigh, interval);
	}

	private void showPermissionRequestText() {
		binding.textView.setText(R.string.MessageNOK);
	}

	@Override
	protected void onResume() {
		super.onResume();

		refreshBatteryStatus();
		resizeDonateButton();
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

		return super.dispatchTouchEvent(event);
	}

	@Override
	public void onRequestPermissionsResult
		(
			int               request,
			@NonNull String[] permissions,
			@NonNull int[]    results
		) {

		if (request != 0) {
			return;
		}

		int granted = PackageManager.PERMISSION_GRANTED;
		if (results.length > 0 && results[0] == granted) {
			NotificationChannelUtil.createChannels(this);
			AlarmReceiver.start(this, batteryLow, batteryHigh, interval);
		} else {
			showPermissionRequestText();
		}
	}
}
