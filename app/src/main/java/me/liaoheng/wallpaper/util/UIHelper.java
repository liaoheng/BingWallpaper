package me.liaoheng.wallpaper.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;

import com.github.liaoheng.common.util.BitmapUtils;
import com.github.liaoheng.common.util.MD5Utils;
import com.github.liaoheng.common.util.ROM;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import me.liaoheng.wallpaper.model.Config;

/**
 * @author liaoheng
 * @version 2018-12-24 11:13
 */
public class UIHelper implements IUIHelper {

    @Override
    public void setWallpaper(Context context, @NonNull Config config, File wallpaper) throws IOException {
        File home = wallpaper;
        File lock = wallpaper;
        if (config.getStackBlur() > 0) {
            File blurFile = WallpaperUtils.getImageStackBlurFile(config.getStackBlur(), wallpaper);
            if (config.getStackBlurMode() == Constants.EXTRA_SET_WALLPAPER_MODE_BOTH) {
                home = blurFile;
                lock = blurFile;
            } else if (config.getStackBlurMode() == Constants.EXTRA_SET_WALLPAPER_MODE_HOME) {
                home = blurFile;
            } else if (config.getStackBlurMode() == Constants.EXTRA_SET_WALLPAPER_MODE_LOCK) {
                lock = blurFile;
            }
        }
        int mode = config.getWallpaperMode();
        if (ROM.getROM().isMiui()) {
            MiuiHelper.setWallpaper(context, mode, home, lock);
        } else if (ROM.getROM().isEmui()) {
            EmuiHelper.setWallpaper(context, mode, home, lock);
        } else {
            try {
                if (TextUtils.isEmpty(getOneUiVersion(context))) {
                    OneUiHelper.setWallpaper(context, mode, home, lock);
                    return;
                }
            } catch (Exception ignored) {
            }
            systemSetWallpaper(context, mode, home, lock);
        }
    }

    //https://stackoverflow.com/questions/60122037/how-can-i-detect-samsung-one-ui
    public static String getOneUiVersion(Context context) throws Exception {
        if (!isSemAvailable(context)) {
            return ""; // was "1.0" originally but probably just a dummy value for one UI devices
        }
        Field semPlatformIntField = Build.VERSION.class.getDeclaredField("SEM_PLATFORM_INT");
        int version = semPlatformIntField.getInt(null) - 90000;
        if (version < 0) {
            // not one ui (could be previous Samsung OS)
            return "";
        }
        return (version / 10000) + "." + ((version % 10000) / 100);
    }

    public static boolean isSemAvailable(Context context) {
        return context != null &&
                (context.getPackageManager().hasSystemFeature("com.samsung.feature.samsung_experience_mobile") ||
                        context.getPackageManager()
                                .hasSystemFeature("com.samsung.feature.samsung_experience_mobile_lite"));
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
        if (options.outHeight < height) {
            isCrop = true;
        }
        if (isCrop) {
            String key = MD5Utils.md5Hex(wallpaper.getAbsolutePath() + "_thumbnail");
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
