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
        if (Settings.isEnableLogProvider(context)) {
            create(context);
        }else{
            LogFileUtils.get().close();
        }
    }

    public static void create(Context context) {
        try{
            LogFileUtils.get().open(context, "log", "");
        }catch (IOException ignored){
        }
    }

    public static void destroy() {
        LogFileUtils.get().close();
        LogFileUtils.get().clearFile();
    }
}
