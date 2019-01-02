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
    public void setWallpaper(Context context, int mode, File wallpaper) throws Exception {
        if (ROM.getROM().isMiui()) {
            MiuiHelper.setWallpaper(context, mode, wallpaper);
        } else if (ROM.getROM().isEmui()) {
            EmuiHelper.setWallpaper(context, mode, wallpaper);
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                BingWallpaperUtils.setWallpaper(context, wallpaper);
            } else {
                if (mode == Constants.EXTRA_SET_WALLPAPER_MODE_HOME) {
                    BingWallpaperUtils.setHomeScreenWallpaper(context, wallpaper);
                } else if (mode == Constants.EXTRA_SET_WALLPAPER_MODE_LOCK) {
                    BingWallpaperUtils.setLockScreenWallpaper(context, wallpaper);
                } else {
                    BingWallpaperUtils.setBothWallpaper(context, wallpaper);
                }
            }
        }
    }
}
