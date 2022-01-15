package me.liaoheng.wallpaper.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;

import com.github.liaoheng.common.util.AppUtils;
import com.github.liaoheng.common.util.BitmapUtils;
import com.github.liaoheng.common.util.ROM;

import java.io.File;
import java.io.IOException;

import me.liaoheng.wallpaper.model.Config;
import me.liaoheng.wallpaper.model.WallpaperImage;

/**
 * @author liaoheng
 * @version 2018-12-24 11:13
 */
public class UIHelper implements IUIHelper {

    @Override
    public void setWallpaper(Context context, @NonNull Config config, File wallpaper, @NonNull String url)
            throws IOException {
        if (WallpaperUtils.isNotSupportedWallpaper(context)) {
            throw new IOException("This device not support wallpaper");
        }
        WallpaperImage image = WallpaperUtils.getImageStackBlurFile(config, wallpaper, url);
        File home = image.getHome();
        File lock = image.getLock();
        int mode = config.getWallpaperMode();
        if (ROM.getROM().isMiui()) {
            MiuiHelper.setWallpaper(context, mode, home, lock);
        } else if (ROM.getROM().isEmui()) {
            EmuiHelper.setWallpaper(context, mode, home, lock);
        } else if (ROM.getROM().isOneUi()) {
            OneUiHelper.setWallpaper(context, mode, home, lock);
        } else {
            systemSetWallpaper(context, mode, home, lock);
        }
    }

    private void systemSetWallpaper(Context context, @Constants.setWallpaperMode int mode, File home,
            File lock) throws IOException {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            AppUtils.setWallpaper(context, home);
        } else {
            if (mode == Constants.EXTRA_SET_WALLPAPER_MODE_HOME) {
                AppUtils.setHomeScreenWallpaper(context, home);
            } else if (mode == Constants.EXTRA_SET_WALLPAPER_MODE_LOCK) {
                AppUtils.setLockScreenWallpaper(context, lock);
            } else {
                AppUtils.setHomeScreenWallpaper(context, home);
                AppUtils.setLockScreenWallpaper(context, lock);
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

    public static File cropWallpaper(Context context, File wallpaper) throws IOException {
        return cropWallpaper(context, wallpaper, true);
    }

    public static File cropWallpaper(Context context, File wallpaper, boolean portrait) throws IOException {
        DisplayMetrics size = BingWallpaperUtils.getSysResolution(context);
        int width = size.widthPixels;
        int height = size.heightPixels;
        if (width == 0 || height == 0) {
            throw new IOException("WindowManager is null");
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(wallpaper.getAbsolutePath(), options);
        boolean isCrop = false;
        if (portrait && (width > height)) {//ensure portrait
            int tmp = width;
            width = height;
            height = tmp;
        }
        if (options.outHeight > height) {
            isCrop = true;
        }
        if (isCrop) {
            String key = BingWallpaperUtils.createKey(wallpaper.getAbsolutePath() + "_thumbnail");
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
}
