package me.liaoheng.wallpaper.util;

import android.content.Context;

/**
 * @author liaoheng
 * @version 2018-12-28 15:11
 */
public class CacheUtils {
    private CacheUtils() {}

    public static com.github.liaoheng.common.util.CacheUtils get() {
        return com.github.liaoheng.common.util.CacheUtils.get();
    }

    public static void init(Context context) {
        get().init(context, "temp");
    }

}
