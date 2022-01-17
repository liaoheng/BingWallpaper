package me.liaoheng.wallpaper.util;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.github.liaoheng.common.util.AppUtils;

import java.io.File;
import java.io.IOException;

import me.liaoheng.wallpaper.model.WallpaperImage;

/**
 * @author liaoheng
 * @version 2020-05-27 11:53
 */
public class OneUiHelper {

    public static void setWallpaper(Context context, @Constants.setWallpaperMode int mode, WallpaperImage image)
            throws IOException {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            AppUtils.setWallpaper(context, image.getHome());
        } else {
            if (mode == Constants.EXTRA_SET_WALLPAPER_MODE_HOME) {
                homeSetWallpaper(context, image.getHome());
            } else if (mode == Constants.EXTRA_SET_WALLPAPER_MODE_LOCK) {
                AppUtils.setLockScreenWallpaper(context, image.getLock());
            } else {
                homeSetWallpaper(context, image.getHome());
                AppUtils.setLockScreenWallpaper(context, image.getLock());
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static void homeSetWallpaper(Context context, File wallpaper) throws IOException {
        //wallpaper = UIHelper.cropWallpaper(context, wallpaper,false);
        AppUtils.setHomeScreenWallpaper(context, wallpaper);
    }
}
