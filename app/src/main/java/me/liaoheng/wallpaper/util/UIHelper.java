package me.liaoheng.wallpaper.util;

import android.content.Context;
import android.os.Build;
import com.github.liaoheng.common.util.ROM;

import java.io.File;

/**
 * @author liaoheng
 * @version 2018-12-24 11:13
 */
public class UIHelper implements IUIHelper {

    @Override
    public boolean setWallpaper(Context context, int mode, File wallpaper) throws Exception {
        if (ROM.getROM().isMiui()) {
            return MiuiHelper.setWallpaper(context, mode, wallpaper);
        } else if (ROM.getROM().isEmui()) {
            return EmuiHelper.setWallpaper(context, mode, wallpaper);
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                return BingWallpaperUtils.setWallpaper(context, wallpaper);
            } else {
                if (mode == Constants.EXTRA_SET_WALLPAPER_MODE_HOME) {
                    return BingWallpaperUtils.setHomeScreenWallpaper(context, wallpaper);
                } else if (mode == Constants.EXTRA_SET_WALLPAPER_MODE_LOCK) {
                    return BingWallpaperUtils.setLockScreenWallpaper(context, wallpaper);
                } else {
                    return BingWallpaperUtils.setBothWallpaper(context, wallpaper);
                }
            }
        }
    }
}
