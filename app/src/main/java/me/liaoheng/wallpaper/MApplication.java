package me.liaoheng.wallpaper;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.github.liaoheng.common.Common;
import com.google.firebase.analytics.FirebaseAnalytics;

import io.fabric.sdk.android.Fabric;
import me.liaoheng.wallpaper.util.BingWallpaperAlarmManager;
import me.liaoheng.wallpaper.util.BingWallpaperJobManager;
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
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (!BuildConfig.DEBUG) {
            Crashlytics.log("onTrimMemory level : " + level);
        }
    }

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
