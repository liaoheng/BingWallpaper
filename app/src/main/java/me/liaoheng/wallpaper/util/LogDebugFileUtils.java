package me.liaoheng.wallpaper.util;

import android.content.Context;

import com.github.liaoheng.common.util.LogFileUtils;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

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
        Observable.just("").subscribeOn(Schedulers.io()).map((Function<String, Object>) s -> {
            LogFileUtils.get().open(context, "log", "");
            return "";
        }).subscribe();
    }

    public static void destroy() {
        Observable.just("").subscribeOn(Schedulers.io()).map((Function<String, Object>) s -> {
            LogFileUtils.get().close();
            LogFileUtils.get().clearFile();
            return "";
        }).subscribe();
    }
}
