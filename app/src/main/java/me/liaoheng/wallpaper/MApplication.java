package me.liaoheng.wallpaper;

import android.app.Application;
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
        }
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(!BuildConfig.DEBUG);

        if (TasksUtils.isOne()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        Constants.FOREGROUND_SET_NOTIFICATION_CHANNEL, "AutoSetWallpaperIntentService",
                        NotificationManager.IMPORTANCE_LOW);
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (manager == null) {
                    return;
                }
                manager.createNotificationChannel(channel);
            }
        }
    }
}
