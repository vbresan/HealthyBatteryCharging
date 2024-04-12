package biz.binarysolutions.healthybatterycharging.receivers;

import static biz.binarysolutions.healthybatterycharging.util.NotificationChannelUtil.COLOR_HIGH;
import static biz.binarysolutions.healthybatterycharging.util.NotificationChannelUtil.COLOR_LOW;
import static biz.binarysolutions.healthybatterycharging.util.NotificationChannelUtil.MORSE_C;
import static biz.binarysolutions.healthybatterycharging.util.NotificationChannelUtil.MORSE_D;
import static biz.binarysolutions.healthybatterycharging.util.NotificationChannelUtil.NOTIFICATION_CHANNEL_ID_HIGH;
import static biz.binarysolutions.healthybatterycharging.util.NotificationChannelUtil.NOTIFICATION_CHANNEL_ID_LOW;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;

import biz.binarysolutions.healthybatterycharging.R;
import biz.binarysolutions.healthybatterycharging.util.IntentUtil;
import biz.binarysolutions.healthybatterycharging.util.Logger;
import biz.binarysolutions.healthybatterycharging.util.NotificationChannelUtil;

/**
 *
 */
class Notifications {

    private static final String TAG = Notifications.class.getSimpleName();

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

        Notification.Builder builder = new Notification.Builder(context, channelId);
        setCommonParameters(context, text, builder);

        return builder.build();
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

        int lightOn  = 1000;
        int lightOff = 618;
        Uri ringtone = NotificationChannelUtil.getRingtone();

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
    static void displayDisconnectChargerNotification(Context context) {

        Logger.d(TAG, "displayDisconnectChargerNotification called");

        String message = context.getString(R.string.DisconnectCharger);

        Notification notification;
        if (Build.VERSION.SDK_INT >= 26) {
            notification = getNotification(context, message, NOTIFICATION_CHANNEL_ID_HIGH);
        } else {
            notification = getNotification(context, message, COLOR_HIGH, MORSE_D);
        }

        notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
        getManager(context).notify(0, notification);
    }

    /**
     *
     * @param context
     */
    static void displayConnectChargerNotification(Context context) {

        Logger.d(TAG, "displayConnectChargerNotification called");

        String message = context.getString(R.string.ConnectCharger);

        Notification notification;
        if (Build.VERSION.SDK_INT >= 26) {
            notification = getNotification(context, message, NOTIFICATION_CHANNEL_ID_LOW);
        } else {
            notification = getNotification(context, message, COLOR_LOW, MORSE_C);
        }

        notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
        getManager(context).notify(0, notification);
    }

    /**
     *
     * @param context
     */
    static void cancellAll(Context context) {

        Logger.d(TAG, "cancellAll called");
        getManager(context).cancelAll();
    }
}
