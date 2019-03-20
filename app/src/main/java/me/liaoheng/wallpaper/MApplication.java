package me.liaoheng.wallpaper;

import android.app.Application;
import com.github.liaoheng.common.Common;
import com.github.liaoheng.common.util.L;
import io.reactivex.plugins.RxJavaPlugins;
import me.liaoheng.wallpaper.util.*;

/**
 * @author liaoheng
 * @version 2016-09-19 11:34
 */
public class MApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Common.baseInit(this, Constants.PROJECT_NAME, BuildConfig.DEBUG);
        L.init(Constants.PROJECT_NAME, BuildConfig.DEBUG);
        TasksUtils.init(this);
        if (BingWallpaperUtils.isEnableLog(this)) {
            LogDebugFileUtils.init(getApplicationContext());
            LogDebugFileUtils.get().open();
        }
        RxJavaPlugins.setErrorHandler(throwable -> L.alog().w("RxJavaPlugins", throwable));
        NetUtils.get().init(getApplicationContext());
        Constants.Config.isPhone = getString(R.string.screen_type).equals("phone");
        WorkerManager.init(this, BuildConfig.DEBUG);

        CrashReportHandle.init(this);

        NotificationUtils.createNotificationChannels(this);
    }
}
