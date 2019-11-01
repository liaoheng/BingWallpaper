package me.liaoheng.wallpaper.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;

import com.github.liaoheng.common.util.BitmapUtils;
import com.github.liaoheng.common.util.DisplayUtils;
import com.github.liaoheng.common.util.MD5Utils;
import com.github.liaoheng.common.util.ShellUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author liaoheng
 * @version 2018-09-19 15:57
 */
public class MiuiHelper {

    public static void setLockScreenWallpaper(Context context, File wallpaper) throws IOException {
        if (!BingWallpaperUtils.isMiuiLockScreenSupport(context)) {
            return;
        }
        if (ShellUtils.hasRootPermission()) {
            int width = DisplayUtils.getScreenInfo(context).widthPixels;
            int height = DisplayUtils.getScreenInfo(context).heightPixels;
            if (width > height) {
                int temp = width;
                width = height;
                height = temp;
            }
            String key = MD5Utils.md5Hex(wallpaper.getAbsolutePath());
            File wallpaperFile = CacheUtils.get().get(key);
            if (wallpaperFile == null) {
                Bitmap newBitmap = ThumbnailUtils.extractThumbnail(
                        BitmapFactory.decodeFile(wallpaper.getAbsolutePath()),
                        width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                if (newBitmap != null) {
                    wallpaper = CacheUtils.get().put(key, BitmapUtils.bitmapToStream(newBitmap,
                            Bitmap.CompressFormat.JPEG));
                }
            } else {
                wallpaper = wallpaperFile;
            }
            setImage(wallpaper);
            return;
        }
        throw new IOException("Not acquired root permission");
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

    public static void setWallpaper(Context context, @Constants.setWallpaperMode int mode, File wallpaper)
            throws IOException {
        if (mode == Constants.EXTRA_SET_WALLPAPER_MODE_HOME) {
            BingWallpaperUtils.setWallpaper(context, wallpaper);
        } else if (mode == Constants.EXTRA_SET_WALLPAPER_MODE_LOCK) {
            setLockScreenWallpaper(context, wallpaper);
        } else {
            try {
                setLockScreenWallpaper(context, wallpaper);
            } catch (Exception ignored) {
            }
            BingWallpaperUtils.setWallpaper(context, wallpaper);
        }
    }
}
