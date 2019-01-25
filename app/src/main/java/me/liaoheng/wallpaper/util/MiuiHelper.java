package me.liaoheng.wallpaper.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Build;
import com.github.liaoheng.common.util.*;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @author liaoheng
 * @version 2018-09-19 15:57
 */
public class MiuiHelper {

    public static void setLockScreenWallpaper(Context context, File file) throws IOException {
        if (BingWallpaperUtils.isMiuiLockScreenSupport(context) && ShellUtils.hasRootPermission()) {
            int width = DisplayUtils.getScreenInfo(context).widthPixels;
            int height = DisplayUtils.getScreenInfo(context).heightPixels;
            File wallpaperFile = null;
            try {
                Bitmap newBitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(file.getAbsolutePath()),
                        width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                wallpaperFile = new File(FileUtils.getProjectSpaceTempDirectory(context),
                        UUID.randomUUID().toString());
                FileUtils.copyToFile(BitmapUtils.bitmapToStream(newBitmap, Bitmap.CompressFormat.JPEG),
                        wallpaperFile);
                BitmapUtils.recycle(newBitmap);
                setImage(wallpaperFile);
            } catch (Exception e) {
                L.alog().e("MiuiHelper", e);
            } finally {
                if (wallpaperFile != null) {
                    FileUtils.delete(wallpaperFile);
                }
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                BingWallpaperUtils.setLockScreenWallpaper(context, file);
            }
        }
    }

    private static void setImage(File file) {
        ShellUtils.CommandResult commandResult = ShellUtils.execCommand(
                "cp " + file.getAbsolutePath() + " /data/system/theme/lock_wallpaper", true, true);
        if (commandResult.result == 0) {
            ShellUtils.execCommand("chmod 755 /data/system/theme/lock_wallpaper", true);
        }
    }

    public static void setWallpaper(Context context, @Constants.setWallpaperMode int mode, File wallpaper)
            throws Exception {
        if (!ROM.getROM().isMiui()) {
            return;
        }
        if (mode == Constants.EXTRA_SET_WALLPAPER_MODE_HOME) {
            BingWallpaperUtils.setWallpaper(context, wallpaper);
        } else if (mode == Constants.EXTRA_SET_WALLPAPER_MODE_LOCK) {
            setLockScreenWallpaper(context, wallpaper);
        } else {
            BingWallpaperUtils.setWallpaper(context, wallpaper);
            setLockScreenWallpaper(context, wallpaper);
        }
    }
}
