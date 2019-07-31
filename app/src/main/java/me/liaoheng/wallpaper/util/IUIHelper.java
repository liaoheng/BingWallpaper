package me.liaoheng.wallpaper.util;

import android.content.Context;
import me.liaoheng.wallpaper.model.Config;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author liaoheng
 * @version 2018-12-24 11:11
 */
public interface IUIHelper {
    boolean setWallpaper(Context context, @Constants.setWallpaperMode int mode, @NotNull Config config, File wallpaper)
            throws Exception;

    void register(Context context, BottomViewListener listener);

    void unregister(Context context);
}
