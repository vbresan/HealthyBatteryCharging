package biz.binarysolutions.healthybatterycharging.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import biz.binarysolutions.healthybatterycharging.BuildConfig;
import biz.binarysolutions.healthybatterycharging.R;

/**
 *
 */
public class NotificationChannelUtil {

    private static final int MORSE_SHORT = 125;
    private static final int MORSE_LONG  = 3 * MORSE_SHORT;
    public static final long[] MORSE_C = new long[] {
        0,
        MORSE_LONG,  MORSE_SHORT,
        MORSE_SHORT, MORSE_SHORT,
        MORSE_LONG,  MORSE_SHORT,
        MORSE_SHORT,
    };
    public static final long[] MORSE_D = new long[] {
        0,
        MORSE_LONG,  MORSE_SHORT,
        MORSE_SHORT, MORSE_SHORT,
        MORSE_SHORT, MORSE_SHORT,
    };

    public static final int COLOR_LOW   = Color.RED;
    public static final int COLOR_HIGH  = Color.GREEN;

    private static final String PACKAGE_NAME = BuildConfig.APPLICATION_ID;
    public static final String NOTIFICATION_CHANNEL_ID_LOW  =
        PACKAGE_NAME + ".channel.low";
    public static final String NOTIFICATION_CHANNEL_ID_HIGH =
        PACKAGE_NAME + ".channel.high";

    /**
     * @param channelId
     * @param channelName
     * @param vibrationPattern
     * @param color
     * @param sound
     * @param audioAttributes
     * @return
     */
    private static NotificationChannel getNotificationChannel
        (
            String          channelId,
            String          channelName,
            long[]          vibrationPattern,
            int             color,
            Uri             sound,
            AudioAttributes audioAttributes
        ) {

        if (Build.VERSION.SDK_INT < 26) {
            return null;
        }

        NotificationChannel channel = new NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        );
        channel.setVibrationPattern(vibrationPattern);
        channel.enableLights(true);
        channel.setLightColor(color);
        channel.setSound(sound, audioAttributes);
        channel.setImportance(NotificationManager.IMPORTANCE_HIGH);

        return channel;
    }

    /**
     *
     * @return
     */
    public static Uri getRingtone() {
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    }

    /**
     *
     * @param context
     */
    public static void createChannels(Context context) {

        if (Build.VERSION.SDK_INT < 26) {
            return;
        }

        Uri sound = getRingtone();
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build();

        NotificationChannel channelLow = getNotificationChannel(
            NOTIFICATION_CHANNEL_ID_LOW,
            context.getString(R.string.BatteryLowNotification),
            MORSE_C,
            COLOR_LOW,
            sound,
            audioAttributes
        );

        NotificationChannel channelHigh = getNotificationChannel(
            NOTIFICATION_CHANNEL_ID_HIGH,
            context.getString(R.string.BatteryHighNotification),
            MORSE_D,
            COLOR_HIGH,
            sound,
            audioAttributes
        );

        if (channelLow == null || channelHigh == null) {
            // they can't be null, but let's get rid of the compiler warning
            return;
        }

        NotificationManager manager =
            context.getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channelLow);
        manager.createNotificationChannel(channelHigh);
    }
}
