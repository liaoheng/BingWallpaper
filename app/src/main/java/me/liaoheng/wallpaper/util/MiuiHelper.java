package me.liaoheng.wallpaper.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.ThumbnailUtils;
import android.os.Build;

import com.github.liaoheng.common.util.BitmapUtils;
import com.github.liaoheng.common.util.MD5Utils;
import com.github.liaoheng.common.util.ShellUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author liaoheng
 * @version 2018-09-19 15:57
 */
public class MiuiHelper {

    @SuppressWarnings({ "WeakerAccess" })
    public static void setLockScreenWallpaper(Context context, File wallpaper) throws IOException {
        if (ShellUtils.hasRootPermission()) {
            setImage(cropWallpaper(context, wallpaper));
            return;
        }
        throw new IOException("Not acquired root permission");
    }

    private static File cropWallpaper(Context context, File wallpaper) throws IOException {
        Point size = BingWallpaperUtils.getSysResolution(context);
        int width = size.x;
        int height = size.y;
        if (width == 0 || height == 0) {
            throw new IOException("WindowManager is null");
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(wallpaper.getAbsolutePath(), options);
        boolean isCrop = false;
        if (width > height) {//ensure portrait
            int tmp = width;
            width = height;
            height = tmp;
        }
        if (options.outHeight < height) {
            isCrop = true;
        }
        if (isCrop) {
            String key = MD5Utils.md5Hex(wallpaper.getAbsolutePath()+"_thumbnail");
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
        }
        return wallpaper;
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

    public static void setWallpaper(Context context, @Constants.setWallpaperMode int mode, File homeWallpaper,
            File lockWallpaper)
            throws IOException {
        if (mode == Constants.EXTRA_SET_WALLPAPER_MODE_HOME) {
            homeSetWallpaper(context, homeWallpaper);
        } else if (mode == Constants.EXTRA_SET_WALLPAPER_MODE_LOCK) {
            lockSetWallpaper(context, lockWallpaper);
        } else {
            homeSetWallpaper(context, homeWallpaper);
            try {
                lockSetWallpaper(context, lockWallpaper);
            } catch (Exception ignored) {
            }
        }
    }

    private static void lockSetWallpaper(Context context, File wallpaper) throws IOException {
        if (!BingWallpaperUtils.isMiuiLockScreenSupport(context)) {
            throw new IOException("Not enable lock screen support");
        }
        setLockScreenWallpaper(context, wallpaper);
    }

    private static void homeSetWallpaper(Context context, File wallpaper) throws IOException {
        wallpaper = cropWallpaper(context, wallpaper);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            BingWallpaperUtils.setHomeScreenWallpaper(context, wallpaper);
        } else {
            BingWallpaperUtils.setWallpaper(context, wallpaper);
        }
    }
}
