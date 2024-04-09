package biz.binarysolutions.healthybatterycharging.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;

import biz.binarysolutions.healthybatterycharging.BuildConfig;
import biz.binarysolutions.healthybatterycharging.R;

/**
 *
 */
public class Notifications {

    private static final int MORSE_SHORT = 125;
    private static final int MORSE_LONG  = 3 * MORSE_SHORT;
    private static final long[] MORSE_C = new long[] {
        0,
        MORSE_LONG,  MORSE_SHORT,
        MORSE_SHORT, MORSE_SHORT,
        MORSE_LONG,  MORSE_SHORT,
        MORSE_SHORT,
    };
    private static final long[] MORSE_D = new long[] {
        0,
        MORSE_LONG,  MORSE_SHORT,
        MORSE_SHORT, MORSE_SHORT,
        MORSE_SHORT, MORSE_SHORT,
    };

    private static final String PACKAGE_NAME = BuildConfig.APPLICATION_ID;
    private static final String NOTIFICATION_CHANNEL_ID_LOW  = PACKAGE_NAME + ".low";
    private static final String NOTIFICATION_CHANNEL_ID_HIGH = PACKAGE_NAME + ".high";

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
     */
    private static void setCommonParameters
        (
            Context              context,
            String               text,
            Notification.Builder builder
        ) {

        String        title       = context.getString(R.string.app_name);
        PendingIntent dummyIntent = IntentUtil.getDummyIntent(context);

        builder.setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setAutoCancel(true)
            .setContentIntent(dummyIntent);
    }

    /**
     *
     * @param context
     * @param text
     * @param channelId
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static Notification getNotification
        (
            Context context,
            String  text,
            String  channelId
        ) {

        System.out.println("HBC ===> Notification.getNotification called");

        Notification.Builder builder = new Notification.Builder(context, channelId);
        setCommonParameters(context, text, builder);

        return builder.build();
    }

    /**
     *
     * @return
     */
    private static Uri getRingtone() {
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    }

    /**
     *
     * @param context
     * @param text
     * @param color
     * @param vibratePattern
     */
    private static Notification getNotification
        (
            Context context,
            String  text,
            int     color,
            long[]  vibratePattern
        ) {

        System.out.println("HBC ===> Notifications.getNotification called");

        int lightOn  = 1000;
        int lightOff = 618;
        Uri ringtone = getRingtone();

        Notification.Builder builder = new Notification.Builder(context);
        setCommonParameters(context, text, builder);

        builder.setVibrate(vibratePattern)
            .setLights(color, lightOn, lightOff)
            .setSound(ringtone);

        Notification notification;
        if (android.os.Build.VERSION.SDK_INT >= 16) {
            builder.setPriority(Notification.PRIORITY_MAX);
            notification = builder.build();
        } else {
            notification = builder.getNotification();
        }

        return notification;
    }

    /**
     *
     * @param context
     * @return
     */
    private static NotificationManager getManager(Context context) {

        if (Build.VERSION.SDK_INT >= 23) {
            return context.getSystemService(NotificationManager.class);
        } else {
            return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
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
            Color.RED,
            sound,
            audioAttributes
        );

        NotificationChannel channelHigh = getNotificationChannel(
            NOTIFICATION_CHANNEL_ID_HIGH,
            context.getString(R.string.BatteryHighNotification),
            MORSE_D,
            Color.GREEN,
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

    /**
     *
     * @param context
     */
    public static void displayDisconnectChargerNotification(Context context) {

        System.out.println("HBC ===> Notifications.displayDisconnectChargerNotification called");

        String message = context.getString(R.string.DisconnectCharger);

        Notification notification;
        if (Build.VERSION.SDK_INT >= 26) {
            notification = getNotification(context, message, NOTIFICATION_CHANNEL_ID_HIGH);
        } else {
            notification = getNotification(context, message, Color.GREEN, MORSE_D);
        }

        notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
        getManager(context).notify(0, notification);
    }

    /**
     *
     * @param context
     */
    public static void displayConnectChargerNotification(Context context) {

        System.out.println("HBC ===> Notifications.displayConnectChargerNotification called");

        String message = context.getString(R.string.ConnectCharger);

        Notification notification;
        if (Build.VERSION.SDK_INT >= 26) {
            notification = getNotification(context, message, NOTIFICATION_CHANNEL_ID_LOW);
        } else {
            notification = getNotification(context, message, Color.RED, MORSE_C);
        }

        notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
        getManager(context).notify(0, notification);
    }

    /**
     *
     * @param context
     */
    public static void cancellAll(Context context) {

        System.out.println("HBC ===> Notifications.cancellAll called");
        getManager(context).cancelAll();
    }
}
