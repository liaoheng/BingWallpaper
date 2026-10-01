package me.liaoheng.wallpaper;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Process;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.startup.AppInitializer;
import androidx.work.Configuration;

import com.github.liaoheng.common.Common;
import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.LanguageContextWrapper;

import net.danlew.android.joda.JodaTimeInitializer;

import java.util.List;

import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import me.liaoheng.wallpaper.data.db.DBHelper;
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
        TasksUtils.init(getApplicationContext());
        // :live_wallpaper and :background don't need DB migration, Sentry or Firebase, and paying for them
        // on every wake is wasteful. NetUtils and CacheUtils are still needed there (JSON fetch, image cache).
        boolean isMainProcess = isMainProcess(this);
        new Thread(() -> {
            NetUtils.get().init(getApplicationContext());
            CacheUtils.init(getApplicationContext());
            if (isMainProcess) {
                DBHelper.toChangeDataStore(getApplicationContext());
                CrashReportHandle.init(getApplicationContext());
            }
        }).start();
        RxJavaPlugins.setErrorHandler(throwable -> L.alog().w("RxJavaPlugins", throwable));
        Constants.Config.isPhone = getString(R.string.screen_type).equals("phone");

        NotificationUtils.createNotificationChannels(this);
    }

    private static boolean isMainProcess(Context context) {
        String processName = getProcessName(context);
        // Unknown process name: assume main so nothing needed is skipped.
        return processName == null || processName.equals(context.getPackageName());
    }

    @Nullable
    private static String getProcessName(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return Application.getProcessName();
        }
        int pid = Process.myPid();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) {
            return null;
        }
        List<ActivityManager.RunningAppProcessInfo> processes = activityManager.getRunningAppProcesses();
        if (processes == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo process : processes) {
            if (process.pid == pid) {
                return process.processName;
            }
        }
        return null;
    }

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return WorkerManager.getConfig(BuildConfig.DEBUG);
    }
}
