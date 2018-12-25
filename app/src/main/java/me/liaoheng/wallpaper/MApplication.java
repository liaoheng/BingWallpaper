package me.liaoheng.wallpaper;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import com.github.liaoheng.common.Common;

import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.Constants;
import me.liaoheng.wallpaper.util.CrashReportHandle;
import me.liaoheng.wallpaper.util.LogDebugFileUtils;
import me.liaoheng.wallpaper.util.NetUtils;
import me.liaoheng.wallpaper.util.TasksUtils;

/**
 * @author liaoheng
 * @version 2016-09-19 11:34
 */
public class MApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Common.init(this, Constants.PROJECT_NAME, BuildConfig.DEBUG);
        TasksUtils.init(this);
        if (BingWallpaperUtils.isEnableLog(this)) {
            LogDebugFileUtils.init(getApplicationContext());
            LogDebugFileUtils.get().open();
        }
        NetUtils.get().init(getApplicationContext());
        Constants.Config.isPhone = getString(R.string.screen_type).equals("phone");

        CrashReportHandle.init(this);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager == null) {
                return;
            }
            NotificationChannel channel = new NotificationChannel(
                    Constants.FOREGROUND_INTENT_SERVICE_NOTIFICATION_CHANNEL,
                    getString(R.string.foreground_intent_service_notification_channel),
                    NotificationManager.IMPORTANCE_LOW);
            manager.createNotificationChannel(channel);

            NotificationChannel channel2 = new NotificationChannel(
                    Constants.FOREGROUND_DAEMON_SERVICE_NOTIFICATION_CHANNEL,
                    getString(R.string.foreground_daemon_service_notification_channel),
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
}
