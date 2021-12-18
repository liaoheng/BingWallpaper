package me.liaoheng.wallpaper;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.startup.AppInitializer;
import androidx.work.Configuration;

import com.github.liaoheng.common.Common;
import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.LanguageContextWrapper;

import net.danlew.android.joda.JodaTimeInitializer;

import io.reactivex.plugins.RxJavaPlugins;
import me.liaoheng.wallpaper.util.CacheUtils;
import me.liaoheng.wallpaper.util.Constants;
import me.liaoheng.wallpaper.util.CrashReportHandle;
import me.liaoheng.wallpaper.util.LogDebugFileUtils;
import me.liaoheng.wallpaper.util.NetUtils;
import me.liaoheng.wallpaper.util.NotificationUtils;
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
        TasksUtils.init(this);
        LogDebugFileUtils.init(this);
        CacheUtils.init(this);
        RxJavaPlugins.setErrorHandler(throwable -> L.alog().w("RxJavaPlugins", throwable));
        NetUtils.get().init(getApplicationContext());
        Constants.Config.isPhone = getString(R.string.screen_type).equals("phone");

        CrashReportHandle.init(this);

        NotificationUtils.createNotificationChannels(this);
    }

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return WorkerManager.getConfig(BuildConfig.DEBUG);
    }
}
