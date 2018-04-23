package me.liaoheng.wallpaper.util;

import com.crashlytics.android.Crashlytics;

import me.liaoheng.wallpaper.BuildConfig;

/**
 * @author liaoheng
 * @version 2018-04-23 23:25
 */
public class ExceptionHandle {
    public static void collectException(String msg, Throwable t) {
        if (BuildConfig.DEBUG) {
            return;
        }
        Crashlytics.log(msg + " : " + t.getMessage());
    }
}
