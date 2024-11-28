package me.liaoheng.wallpaper;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.startup.AppInitializer;
import androidx.work.Configuration;

import com.github.liaoheng.common.Common;
import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.LanguageContextWrapper;

import net.danlew.android.joda.JodaTimeInitializer;

import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import me.liaoheng.wallpaper.util.CacheUtils;
import me.liaoheng.wallpaper.util.Constants;
import me.liaoheng.wallpaper.util.CrashReportHandle;
import me.liaoheng.wallpaper.util.LogDebugFileUtils;
import me.liaoheng.wallpaper.util.NetUtils;
import me.liaoheng.wallpaper.util.NotificationUtils;
import me.liaoheng.wallpaper.util.SettingTrayPreferences;
import me.liaoheng.wallpaper.util.TasksUtils;
import me.liaoheng.wallpaper.util.WorkerManager;

/**
 * @author liaoheng
 * @version 2016-09-19 11:34
 */
public class MApplication extends Application implements Configuration.Provider {

    @Override
    public void onCreate() {
        super.onCreate();
        LanguageContextWrapper.init(this);
        Common.init(this, Constants.PROJECT_NAME, BuildConfig.DEBUG);
        AppInitializer.getInstance(this).initializeComponent(JodaTimeInitializer.class);
        SettingTrayPreferences.init(getApplicationContext());
        LogDebugFileUtils.init(getApplicationContext());
        new Thread(() -> {
            TasksUtils.init(getApplicationContext());
            CacheUtils.init(getApplicationContext());
            NetUtils.get().init(getApplicationContext());
            CrashReportHandle.init(getApplicationContext());
        }).start();
        RxJavaPlugins.setErrorHandler(throwable -> L.alog().w("RxJavaPlugins", throwable));
        Constants.Config.isPhone = getString(R.string.screen_type).equals("phone");


        NotificationUtils.createNotificationChannels(this);
    }

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return WorkerManager.getConfig(BuildConfig.DEBUG);
    }
}
