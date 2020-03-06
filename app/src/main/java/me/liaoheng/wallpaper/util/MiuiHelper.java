package me.liaoheng.wallpaper.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.ThumbnailUtils;
import android.view.WindowManager;

import androidx.core.content.ContextCompat;

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

    @SuppressWarnings({ "WeakerAccess", "SuspiciousNameCombination" })
    public static void setLockScreenWallpaper(Context context, File wallpaper) throws IOException {
        if (ShellUtils.hasRootPermission()) {
            WindowManager wm = ContextCompat.getSystemService(context, WindowManager.class);
            if (wm == null) {
                throw new IOException("WindowManager is null");
            }
            Point size = new Point();
            wm.getDefaultDisplay().getRealSize(size);
            int width = size.x;
            int height = size.y;

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(wallpaper.getAbsolutePath(), options);
            boolean isCrop = false;
            if (options.outWidth > options.outHeight) {//horizontal
                if (options.outHeight < height) {
                    isCrop = true;
                }
                int tmp = width;
                width = height;
                height = tmp;
            } else {//vertical
                if (options.outWidth < height) {
                    isCrop = true;
                }
            }
            if (isCrop) {
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

    public static void setWallpaper(Context context, @Constants.setWallpaperMode int mode, File homeWallpaper,
            File lockWallpaper)
            throws IOException {
        if (mode == Constants.EXTRA_SET_WALLPAPER_MODE_HOME) {
            systemSetWallpaper(context, homeWallpaper);
        } else if (mode == Constants.EXTRA_SET_WALLPAPER_MODE_LOCK) {
            lockSetWallpaper(context, lockWallpaper);
        } else {
            systemSetWallpaper(context, homeWallpaper);
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

    private static void systemSetWallpaper(Context context, File wallpaper) throws IOException {
        BingWallpaperUtils.setWallpaper(context, wallpaper);
    }
}
