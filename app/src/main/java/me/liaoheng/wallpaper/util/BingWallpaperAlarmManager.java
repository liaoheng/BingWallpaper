package me.liaoheng.wallpaper.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import me.liaoheng.wallpaper.service.AutoSetWallpaperBroadcastReceiver;

/**
 * @author liaoheng
 * @version 2016-09-20 16:25
 */
public class BingWallpaperAlarmManager {

    private static final int REQUEST_CODE = 0x12;

    private static PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(context, AutoSetWallpaperBroadcastReceiver.class);
        intent.setAction(AutoSetWallpaperBroadcastReceiver.ACTION);
        return PendingIntent
                .getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void disabled(Context context) {
        PendingIntent pendingIntent = getPendingIntent(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }
        alarmManager.cancel(pendingIntent);
    }

    public static void enabled(Context context, @NonNull LocalTime localTime) {
        disabled(context);
        add(context, localTime);
    }

    private static void add(Context context, DateTime time) {
        PendingIntent pendingIntent = getPendingIntent(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }
        alarmManager
                .setRepeating(AlarmManager.RTC_WAKEUP, time.getMillis(), AlarmManager.INTERVAL_DAY,
                        pendingIntent);
    }

    private static void add(Context context, @NonNull LocalTime localTime) {
        DateTime dateTime = BingWallpaperUtils.checkTime(localTime);
        add(context, dateTime);
    }
}
