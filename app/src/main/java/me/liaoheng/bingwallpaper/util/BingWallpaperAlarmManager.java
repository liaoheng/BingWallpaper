package me.liaoheng.bingwallpaper.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.github.liaoheng.common.util.L;
import me.liaoheng.bingwallpaper.service.AutoSetWallpaperBroadcastReceiver;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;

/**
 * @author liaoheng
 * @version 2016-09-20 16:25
 */
public class BingWallpaperAlarmManager {

    private static final String TAG            = BingWallpaperAlarmManager.class.getSimpleName();
    public static final  String ACTION         = "me.liaoheng.bingwallpaper.alarm.task_schedule";
    public static final  String ACTION_LIMITED = "me.liaoheng.bingwallpaper.alarm.task_schedule_limited";

    public static final int REQUEST_CODE         = 0x12;
    public static final int REQUEST_CODE_LIMITED = 0x32;

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

        if (Utils.isEnableLog(context)) {
            LogDebugFileUtils.get().i(TAG,"Set Alarm Repeating Time : %s", time.toString("yyyy-MM-dd HH:mm"));
        }
        L.Log.i(TAG, "Set Alarm Repeating Time : %s", time.toString("yyyy-MM-dd HH:mm"));
    }

    public static void add(Context context, int hour, int minute) {
        LocalTime localTime = new LocalTime(hour, minute);
        add(context, localTime);
    }

    public static void add(Context context, LocalTime localTime) {
        DateTime dateTime = Utils.checkTime(localTime);
        add(context, dateTime);
    }

    private static PendingIntent getPendingIntentLimited(Context context) {
        Intent intent = new Intent(context, AutoSetWallpaperBroadcastReceiver.class);
        intent.setAction(ACTION_LIMITED);
        return PendingIntent.getBroadcast(context, REQUEST_CODE_LIMITED, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void addLimited(Context context, DateTime time) {
        PendingIntent pendingIntent = getPendingIntentLimited(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, time.getMillis(), pendingIntent);
    }

    public static void clearLimited(Context context) {
        PendingIntent pendingIntent = getPendingIntentLimited(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}
