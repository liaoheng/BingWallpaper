package me.liaoheng.bingwallpaper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.github.liaoheng.common.util.L;
import org.joda.time.DateTime;

/**
 * @author liaoheng
 * @version 2016-09-20 16:25
 */
public class BingWallpaperAlarmManager {

    private static final String TAG = BingWallpaperAlarmManager.class.getSimpleName();

    public static final int REQUEST_CODE = 0x123;

    private static PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(context, AutoUpdateBroadcastReceiver.class);
        return PendingIntent
                .getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public static void clear(Context context) {
        PendingIntent pendingIntent = getPendingIntent(context);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // 取消以前同类型的提醒
        alarmManager.cancel(pendingIntent);

        L.i(TAG, "cancel alarm");
    }

    public static void add(Context context, int hour, int minute) {
        PendingIntent pendingIntent = getPendingIntent(context);
        DateTime now = DateTime.now();
        DateTime dateTime = new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(),
                hour, minute);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // 设定每天在指定的时间运行alert
        alarmManager.setRepeating(AlarmManager.RTC, dateTime.getMillis(), AlarmManager.INTERVAL_DAY,
                pendingIntent);

        L.i(TAG, "set alarm Repeating time : %s", dateTime.toString("HH:mm"));
    }
}
