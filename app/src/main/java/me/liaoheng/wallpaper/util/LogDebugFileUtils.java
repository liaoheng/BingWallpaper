package me.liaoheng.wallpaper.util;

import android.content.Context;

import com.github.liaoheng.common.util.LogFileUtils;

import java.io.IOException;

/**
 * 不带system log
 *
 * @author liaoheng
 * @version 2016-09-22 16:26
 */
public class LogDebugFileUtils {

    private LogDebugFileUtils() {
    }

    public static LogFileUtils get() {
        return LogFileUtils.get();
    }

    public static void init(Context context) {
        try {
            LogFileUtils.get().init(context, "log", "");
        } catch (IOException ignored) {
        }
    }
}
