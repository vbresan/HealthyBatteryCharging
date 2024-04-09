package biz.binarysolutions.healthybatterycharging.util;

import android.text.TextWatcher;

/**
 *
 */
public abstract class DefaultTextWatcher implements TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // do nothing
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // do nothing
    }
}
