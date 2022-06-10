package me.liaoheng.wallpaper.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.List;

import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.ui.MainActivity;

/**
 * @author liaoheng
 * @version 2019-01-02 12:38
 */
public class NotificationUtils {

    public static void createNotificationChannels(@NonNull Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            List<NotificationChannel> channels = new ArrayList<>();
            NotificationChannel channel = new NotificationChannel(
                    Constants.FOREGROUND_INTENT_SERVICE_NOTIFICATION_CHANNEL,
                    context.getString(R.string.foreground_intent_service_notification_channel),
                    NotificationManager.IMPORTANCE_LOW);
            channels.add(channel);

            NotificationChannel channel1 = new NotificationChannel(
                    Constants.FOREGROUND_INTENT_SERVICE_SUCCESS_NOTIFICATION_CHANNEL,
                    context.getString(R.string.foreground_intent_service_success_notification_channel),
                    NotificationManager.IMPORTANCE_LOW);
            channel1.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channels.add(channel1);

            NotificationManagerCompat.from(context).createNotificationChannels(channels);
        }
    }

    public static void showSuccessNotification(Context context, String content) {
        Intent resultIntent = new Intent(context, MainActivity.class);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(context, 12, resultIntent,
                        BingWallpaperUtils.getPendingIntentFlag());

        Notification notification = new NotificationCompat.Builder(context,
                Constants.FOREGROUND_INTENT_SERVICE_SUCCESS_NOTIFICATION_CHANNEL).setSmallIcon(
                R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setContentText(content)
                .setContentTitle(context.getText(R.string.set_wallpaper_success))
                .setContentIntent(resultPendingIntent).build();
        NotificationManagerCompat.from(context).notify(222, notification);
    }

    public static void showFailureNotification(Context context) {
        Notification notification = new NotificationCompat.Builder(context,
                Constants.FOREGROUND_INTENT_SERVICE_NOTIFICATION_CHANNEL).setSmallIcon(
                R.drawable.ic_notification)
                .setAutoCancel(true)
                .setContentText(context.getText(R.string.set_wallpaper_failure))
                .setContentTitle(context.getText(R.string.app_name)).build();
        NotificationManagerCompat.from(context).notify(111, notification);
    }

    public static void clearFailureNotification(Context context) {
        NotificationManagerCompat.from(context).cancel(111);
    }

    public static Notification getStartNotification(Context context) {
        return new NotificationCompat.Builder(context,
                Constants.FOREGROUND_INTENT_SERVICE_NOTIFICATION_CHANNEL).setSmallIcon(
                R.drawable.ic_notification)
                .setContentText(context.getText(R.string.set_wallpaper_running))
                .setContentTitle(context.getText(R.string.app_name))
                .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE).build();
    }

    public static void showStartNotification(Service service) {
        service.startForeground(212, getStartNotification(service.getApplicationContext()));
    }

    public static void showStartNotification(Context context) {
        NotificationManagerCompat.from(context).notify(222, getStartNotification(context));
    }
}
