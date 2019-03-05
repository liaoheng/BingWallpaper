package me.liaoheng.wallpaper.util;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.ui.MainActivity;

/**
 * @author liaoheng
 * @version 2019-01-02 12:38
 */
public class NotificationUtils {

    public static void createNotificationChannels(@NonNull Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager == null) {
                return;
            }
            NotificationChannel channel = new NotificationChannel(
                    Constants.FOREGROUND_INTENT_SERVICE_NOTIFICATION_CHANNEL,
                    context.getString(R.string.foreground_intent_service_notification_channel),
                    NotificationManager.IMPORTANCE_LOW);
            manager.createNotificationChannel(channel);

            NotificationChannel channel1 = new NotificationChannel(
                    Constants.FOREGROUND_INTENT_SERVICE_SUCCESS_NOTIFICATION_CHANNEL,
                    context.getString(R.string.foreground_intent_service_success_notification_channel),
                    NotificationManager.IMPORTANCE_LOW);
            channel1.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            manager.createNotificationChannel(channel1);

            NotificationChannel channel2 = new NotificationChannel(
                    Constants.FOREGROUND_DAEMON_SERVICE_NOTIFICATION_CHANNEL,
                    context.getString(R.string.foreground_daemon_service_notification_channel),
                    NotificationManager.IMPORTANCE_MIN);
            channel2.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            manager.createNotificationChannel(channel2);

            NotificationChannel gms = new NotificationChannel(
                    Constants.GMS_NOTIFICATION_CHANNEL,
                    "Google Service Notification",
                    NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(gms);
        }
    }

    public static void showSuccessNotification(Context context, String content) {
        Intent resultIntent = new Intent(context, MainActivity.class);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(context, 12, resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(context,
                Constants.FOREGROUND_INTENT_SERVICE_SUCCESS_NOTIFICATION_CHANNEL).setSmallIcon(
                R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setContentText(content)
                .setContentTitle(context.getText(R.string.set_wallpaper_success))
                .setContentIntent(resultPendingIntent).build();
        NotificationManagerCompat.from(context).notify(12, notification);
    }

    public static void showFailureNotification(Context context) {
        Notification notification = new NotificationCompat.Builder(context,
                Constants.FOREGROUND_INTENT_SERVICE_NOTIFICATION_CHANNEL).setSmallIcon(
                R.drawable.ic_notification)
                .setAutoCancel(true)
                .setContentText(context.getText(R.string.set_wallpaper_failure))
                .setContentTitle(context.getText(R.string.app_name)).build();
        NotificationManagerCompat.from(context).notify(11, notification);
    }

    public static void clearFailureNotification(Context context) {
        NotificationManagerCompat.from(context).cancel(11);
    }

    public static void showStartNotification(Service service) {
        Notification notification = new NotificationCompat.Builder(service.getApplicationContext(),
                Constants.FOREGROUND_INTENT_SERVICE_NOTIFICATION_CHANNEL).setSmallIcon(
                R.drawable.ic_notification)
                .setContentText(service.getText(R.string.set_wallpaper_running))
                .setContentTitle(service.getText(R.string.app_name)).build();
        service.startForeground(111 + (int) (Math.random() * 10), notification);// Multiple services
    }

    public static void showRunningNotification(Service service) {
        Notification notification = new NotificationCompat.Builder(service.getApplicationContext(),
                Constants.FOREGROUND_DAEMON_SERVICE_NOTIFICATION_CHANNEL).setPriority(NotificationCompat.PRIORITY_MIN)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(service.getString(R.string.app_name))
                .setContentText(service.getString(R.string.daemon_service_running))
                .build();
        service.startForeground(112, notification);
    }

    public static void showGMSErrorNotification(Context context, String errorString) {
        Notification notification = new NotificationCompat.Builder(context,
                Constants.GMS_NOTIFICATION_CHANNEL).setSmallIcon(
                R.drawable.ic_notification)
                .setAutoCancel(true)
                .setContentText(String.format("Google service error: %s", errorString))
                .setContentTitle(context.getString(R.string.app_name))
                .build();
        NotificationManagerCompat.from(context).notify(56, notification);
    }
}
