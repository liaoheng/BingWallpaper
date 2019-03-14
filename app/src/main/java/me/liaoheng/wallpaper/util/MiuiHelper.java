package me.liaoheng.wallpaper.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import com.github.liaoheng.common.util.*;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @author liaoheng
 * @version 2018-09-19 15:57
 */
public class MiuiHelper {

    public static boolean setLockScreenWallpaper(Context context, File file) throws IOException {
        if (BingWallpaperUtils.isMiuiLockScreenSupport(context) && ShellUtils.hasRootPermission()) {
            int width = DisplayUtils.getScreenInfo(context).widthPixels;
            int height = DisplayUtils.getScreenInfo(context).heightPixels;
            if (width > height) {
                int temp = width;
                width = height;
                height = temp;
            }
            File wallpaperFile = null;
            try {
                Bitmap newBitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(file.getAbsolutePath()),
                        width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                wallpaperFile = new File(FileUtils.getProjectSpaceTempDirectory(context),
                        UUID.randomUUID().toString());
                FileUtils.copyToFile(BitmapUtils.bitmapToStream(newBitmap, Bitmap.CompressFormat.JPEG),
                        wallpaperFile);
                BitmapUtils.recycle(newBitmap);
                return setImage(wallpaperFile);
            } catch (Exception e) {
                L.alog().e("MiuiHelper", e);
            } finally {
                if (wallpaperFile != null) {
                    FileUtils.delete(wallpaperFile);
                }
            }
        }
        return false;
    }

    private static boolean setImage(File file) {
        ShellUtils.CommandResult commandResult = ShellUtils.execCommand(
                "cp " + file.getAbsolutePath() + " /data/system/theme/lock_wallpaper", true, true);
        if (commandResult.result == 0) {
            ShellUtils.execCommand("chmod 755 /data/system/theme/lock_wallpaper", true);
            return true;
        } else {
            return false;
        }
    }

    public static boolean setWallpaper(Context context, @Constants.setWallpaperMode int mode, File wallpaper)
            throws Exception {
        if (mode == Constants.EXTRA_SET_WALLPAPER_MODE_HOME) {
            return BingWallpaperUtils.setWallpaper(context, wallpaper);
        } else if (mode == Constants.EXTRA_SET_WALLPAPER_MODE_LOCK) {
            return setLockScreenWallpaper(context, wallpaper);
        } else {
            setLockScreenWallpaper(context, wallpaper);
            return BingWallpaperUtils.setWallpaper(context, wallpaper);
        }
    }
}
