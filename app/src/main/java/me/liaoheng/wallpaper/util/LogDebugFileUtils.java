package me.liaoheng.wallpaper.util;

import android.content.Context;

import com.github.liaoheng.common.util.LogFileUtils;

import java.io.IOException;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

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
        } else {
            LogFileUtils.get().close();
        }
    }

    public static void create(Context context) {
        new Thread(() -> {
            try {
                LogFileUtils.get().open(context, "log", "");
            } catch (IOException ignored) {
            }
        }).start();
    }

    public static void destroy() {
        new Thread(() -> {
            LogFileUtils.get().close();
            LogFileUtils.get().clearFile();
        }).start();
    }
}
