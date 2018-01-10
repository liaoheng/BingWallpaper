package me.liaoheng.bingwallpaper;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.github.liaoheng.common.Common;
import com.google.firebase.analytics.FirebaseAnalytics;

import io.fabric.sdk.android.Fabric;
import me.liaoheng.bingwallpaper.util.BingWallpaperAlarmManager;
import me.liaoheng.bingwallpaper.util.BingWallpaperJobManager;
import me.liaoheng.bingwallpaper.util.BingWallpaperUtils;
import me.liaoheng.bingwallpaper.util.Constants;
import me.liaoheng.bingwallpaper.util.LogDebugFileUtils;
import me.liaoheng.bingwallpaper.util.NetUtils;
import me.liaoheng.bingwallpaper.util.TasksUtils;

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

        //debug firebase not work
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(!BuildConfig.DEBUG);

        if (TasksUtils.isOne()) {
            BingWallpaperAlarmManager.clear(this);
            BingWallpaperJobManager.disabled(this);
        }
    }
}
