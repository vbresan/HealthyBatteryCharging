package biz.binarysolutions.healthybatterycharging.util;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 *
 */
public class IntentUtil {

    /**
     *
     * @param flags
     */
    public static int getPendingIntentFlags(int flags) {

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
    public static PendingIntent getDummyIntent(Context context) {

        PendingIntent intent = PendingIntent.getActivity(
            context,
            0,
            new Intent(),
            getPendingIntentFlags(0)
        );

        return intent;
    }
}
