package me.liaoheng.wallpaper;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.github.liaoheng.common.Common;
import com.google.firebase.analytics.FirebaseAnalytics;

import io.fabric.sdk.android.Fabric;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.Constants;
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
            LogDebugFileUtils.get().init();
            LogDebugFileUtils.get().open();
        }
        NetUtils.get().init();
        Constants.Config.isPhone = getString(R.string.screen_type).equals("phone");

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
            FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true);
        }
        if (TasksUtils.isOne()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        Constants.FOREGROUND_INTENT_SERVICE_NOTIFICATION_CHANNEL,
                        getString(R.string.foreground_intent_service_notification_channel),
                        NotificationManager.IMPORTANCE_LOW);

                NotificationChannel channel2 = new NotificationChannel(
                        Constants.FOREGROUND_DAEMON_SERVICE_NOTIFICATION_CHANNEL,
                        getString(R.string.foreground_daemon_service_notification_channel),
                        NotificationManager.IMPORTANCE_MIN);
                channel2.setLockscreenVisibility(Notification.VISIBILITY_SECRET);

                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (manager == null) {
                    return;
                }
                manager.createNotificationChannel(channel);
                manager.createNotificationChannel(channel2);
            }
        }
    }
}
