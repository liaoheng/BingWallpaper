package me.liaoheng.wallpaper.util;

import android.content.Context;
import android.os.Build;

import com.github.liaoheng.common.util.AppUtils;
import com.github.liaoheng.common.util.ShellUtils;

import java.io.File;
import java.io.IOException;

import me.liaoheng.wallpaper.model.WallpaperImage;

/**
 * @author liaoheng
 * @version 2018-09-19 15:57
 */
public class MiuiHelper {

    @SuppressWarnings({ "WeakerAccess" })
    public static void setLockScreenWallpaper(Context context, WallpaperImage image) throws IOException {
        if (ShellUtils.hasRootPermission()) {
            setImage(UIHelper.cropWallpaper(context, image.getLock(), image.getUrl(), false));
            return;
        }
        throw new LockSetWallpaperException("Not acquired root permission");
    }

    private static void setImage(File file) throws IOException {
        ShellUtils.CommandResult commandResult = ShellUtils.execCommand(
                "cp " + file.getAbsolutePath() + " /data/system/theme/lock_wallpaper", true, true);
        if (commandResult.result == 0) {
            ShellUtils.execCommand("chmod 755 /data/system/theme/lock_wallpaper", true);
        } else {
            throw new IOException("Shell cp error");
        }
    }

    public static void setWallpaper(Context context, @Constants.setWallpaperMode int mode, WallpaperImage image)
            throws IOException {
        if (mode == Constants.EXTRA_SET_WALLPAPER_MODE_HOME) {
            homeSetWallpaper(context, image.getHome());
        } else if (mode == Constants.EXTRA_SET_WALLPAPER_MODE_LOCK) {
            lockSetWallpaper(context, image);
        } else {
            homeSetWallpaper(context, image.getHome());
            try {
                lockSetWallpaper(context, image);
            } catch (Exception ignored) {
            }
        }
    }

    public static void lockSetWallpaper(Context context, WallpaperImage image) throws IOException {
        if (!Settings.isMiuiLockScreenSupport(context)) {
            throw new LockSetWallpaperException("Not enable lock screen support");
        }
        setLockScreenWallpaper(context, image);
    }

    private static void homeSetWallpaper(Context context, File wallpaper) throws IOException {
        //wallpaper = UIHelper.cropWallpaper(context, wallpaper);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            AppUtils.setHomeScreenWallpaper(context, wallpaper);
        } else {
            AppUtils.setWallpaper(context, wallpaper);
        }
    }
}
