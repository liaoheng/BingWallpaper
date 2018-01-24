package me.liaoheng.wallpaper.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.github.liaoheng.common.util.L;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import me.liaoheng.wallpaper.service.AutoSetWallpaperBroadcastReceiver;

/**
 * @author liaoheng
 * @version 2016-09-20 16:25
 */
public class BingWallpaperAlarmManager {

    private static final String TAG = BingWallpaperAlarmManager.class.getSimpleName();
    public static final String ACTION = "me.liaoheng.wallpaper.ALARM_TASK_SCHEDULE";

    public static final int REQUEST_CODE = 0x12;

    private static PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(context, AutoSetWallpaperBroadcastReceiver.class);
        intent.setAction(ACTION);
        return PendingIntent
                .getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void clear(Context context) {
        PendingIntent pendingIntent = getPendingIntent(context);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // 取消以前同类型的提醒
        alarmManager.cancel(pendingIntent);

        L.Log.i(TAG, "cancel alarm");
    }

    public static void add(Context context, DateTime time) {
        PendingIntent pendingIntent = getPendingIntent(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // 设定每天在指定的时间运行alert
        alarmManager
                .setRepeating(AlarmManager.RTC_WAKEUP, time.getMillis(), AlarmManager.INTERVAL_DAY,
                        pendingIntent);

        if (BingWallpaperUtils.isEnableLog(context)) {
            LogDebugFileUtils.get().i(TAG, "Set Alarm Repeating Time : %s", time.toString("yyyy-MM-dd HH:mm"));
        }
        L.Log.i(TAG, "Set Alarm Repeating Time : %s", time.toString("yyyy-MM-dd HH:mm"));
    }

    public static void add(Context context, int hour, int minute) {
        LocalTime localTime = new LocalTime(hour, minute);
        add(context, localTime);
    }

    public static void add(Context context, @NonNull LocalTime localTime) {
        DateTime dateTime = BingWallpaperUtils.checkTime(localTime);
        add(context, dateTime);
    }
}
