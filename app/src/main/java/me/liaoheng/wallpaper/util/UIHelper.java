package me.liaoheng.wallpaper.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.annotation.NonNull;

import com.github.liaoheng.common.util.BitmapUtils;
import com.github.liaoheng.common.util.MD5Utils;
import com.github.liaoheng.common.util.ROM;

import java.io.File;
import java.io.IOException;

import me.liaoheng.wallpaper.model.Config;

/**
 * @author liaoheng
 * @version 2018-12-24 11:13
 */
public class UIHelper implements IUIHelper {

    @Override
    public void setWallpaper(Context context, int mode, @NonNull Config config, File wallpaper) throws IOException {
        File home = wallpaper;
        File lock = wallpaper;
        if (config.getStackBlur() > 0) {
            String key = MD5Utils.md5Hex(wallpaper.getAbsolutePath() + "_" + config.getStackBlur());
            File stackBlurFile = CacheUtils.get().get(key);
            if (stackBlurFile == null) {
                Bitmap stackBlur = BingWallpaperUtils.toStackBlur(
                        BitmapFactory.decodeFile(wallpaper.getAbsolutePath()), config.getStackBlur());
                stackBlurFile = CacheUtils.get().put(key, BitmapUtils.bitmapToStream(stackBlur,
                        Bitmap.CompressFormat.JPEG));
            }
            if (config.getStackBlurMode() == Constants.EXTRA_SET_WALLPAPER_MODE_BOTH) {
                home = stackBlurFile;
                lock = stackBlurFile;
            } else if (config.getStackBlurMode() == Constants.EXTRA_SET_WALLPAPER_MODE_HOME) {
                home = stackBlurFile;
            } else if (config.getStackBlurMode() == Constants.EXTRA_SET_WALLPAPER_MODE_LOCK) {
                lock = stackBlurFile;
            }
        }

        if (ROM.getROM().isMiui()) {
            MiuiHelper.setWallpaper(context, mode, home, lock);
        } else if (ROM.getROM().isEmui()) {
            EmuiHelper.setWallpaper(context, mode, home, lock);
        } else {
            systemSetWallpaper(context, mode, home, lock);
        }
    }

    private void systemSetWallpaper(Context context, @Constants.setWallpaperMode int mode, File home,
            File lock) throws IOException {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            BingWallpaperUtils.setWallpaper(context, home);
        } else {
            if (mode == Constants.EXTRA_SET_WALLPAPER_MODE_HOME) {
                BingWallpaperUtils.setHomeScreenWallpaper(context, home);
            } else if (mode == Constants.EXTRA_SET_WALLPAPER_MODE_LOCK) {
                BingWallpaperUtils.setLockScreenWallpaper(context, lock);
            } else {
                BingWallpaperUtils.setHomeScreenWallpaper(context, home);
                BingWallpaperUtils.setLockScreenWallpaper(context, lock);
            }
        }
    }

    private EmuiHelper mEmuiHelper;

    @Override
    public void register(Context context, BottomViewListener listener) {
        if (ROM.getROM().isEmui()) {
            mEmuiHelper = EmuiHelper.with(listener);
            mEmuiHelper.register(context);
        } else if (ROM.getROM().isVivo()) {
            if (!BingWallpaperUtils.vivoNavigationGestureEnabled(context)) {
                listener.showBottomView();
            }
        } else if (ROM.getROM().isMiui()) {
            if (!BingWallpaperUtils.miuiNavigationGestureEnabled(context)) {
                listener.showBottomView();
            }
        } else {
            listener.showBottomView();
        }
    }

    @Override
    public void unregister(Context context) {
        if (mEmuiHelper != null) {
            mEmuiHelper.unregister(context);
        }
    }
}
