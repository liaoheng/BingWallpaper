package me.liaoheng.wallpaper.util;

import android.content.Context;
import com.github.liaoheng.util.FileUtils;
import java.io.File;
import java.io.IOException;

/**
 * @author liaoheng
 * @version 2018-12-28 15:11
 */
public class CacheUtils {
    private CacheUtils() {}

    public static com.github.liaoheng.util.CacheUtils get() {
        return com.github.liaoheng.util.CacheUtils.get();
    }

    public static void init(Context context) {
        try {
            get().init(getTempFile(context));
        } catch (IOException ignored) {
        }
    }

    public static File getTempFile(Context context) throws IOException {
        return FileUtils.getProjectSpaceCacheDirectory(context, "temp");
    }

}
