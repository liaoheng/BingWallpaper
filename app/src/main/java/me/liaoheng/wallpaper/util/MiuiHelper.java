package me.liaoheng.wallpaper.util;

import android.content.Context;
import android.os.Build;
import com.github.liaoheng.common.util.ROM;
import com.github.liaoheng.common.util.ShellUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author liaoheng
 * @version 2018-09-19 15:57
 */
public class MiuiHelper {

    public static void setLockScreenWallpaper(Context context, File bitmap) throws IOException {
        if (BingWallpaperUtils.isMiuiLockScreenSupport(context) && ShellUtils.hasRootPermission()) {
            ShellUtils.CommandResult commandResult = ShellUtils.execCommand(
                    "cp " + bitmap.getAbsolutePath() + " /data/system/theme/lock_wallpaper", true, true);
            if (commandResult.result == 0) {
                ShellUtils.execCommand("chmod 755 /data/system/theme/lock_wallpaper", true);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                BingWallpaperUtils.setLockScreenWallpaper(context, bitmap);
            }
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
