package me.liaoheng.wallpaper.util;

import android.content.Context;

import java.io.File;

/**
 * @author liaoheng
 * @version 2018-12-24 11:11
 */
public interface IUIHelper {
    boolean setWallpaper(Context context, @Constants.setWallpaperMode int mode, File wallpaper)
            throws Exception;
}
