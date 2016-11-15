package me.liaoheng.bingwallpaper;

import android.app.Application;
import com.github.liaoheng.common.Common;
import jonathanfinerty.once.Once;
import me.liaoheng.bingwallpaper.util.Constants;
import me.liaoheng.bingwallpaper.util.LogDebugFileUtils;
import me.liaoheng.bingwallpaper.util.NetUtils;
import me.liaoheng.bingwallpaper.util.TasksUtils;
import me.liaoheng.bingwallpaper.util.Utils;
import net.danlew.android.joda.JodaTimeAndroid;

/**
 * @author liaoheng
 * @version 2016-09-19 11:34
 */
public class MApplication extends Application {
    @Override public void onCreate() {
        super.onCreate();
        Common.init(this, Constants.PROJECT_NAME, BuildConfig.DEBUG);
        Once.initialise(this);
        TasksUtils.init(this);
        JodaTimeAndroid.init(this);
        if (Utils.isEnableLog(this)) {
            LogDebugFileUtils.get().init("log.txt");
            LogDebugFileUtils.get().open();
        }
        NetUtils.get().init();
    }
}
