package me.liaoheng.wallpaper.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.IOException;

/**
 * @author liaoheng
 * @version 2018-09-18 16:26
 */
public class EmuiHelper {

    private BottomViewListener mListener;
    private BroadcastReceiver mNavigationBarBCR;
    private final String NAVIGATION_BAR_STATUS_CHANGE = "com.huawei.navigationbar.statuschange";

    private EmuiHelper(BottomViewListener listener) {
        mListener = listener;
    }

    public static EmuiHelper with(BottomViewListener listener) {
        return new EmuiHelper(listener);
    }

    public void register(Context context) {
        if (BingWallpaperUtils.emuiNavigationEnabled(context)) {
            mListener.showBottomView();
        }
        mNavigationBarBCR = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null &&
                        (NAVIGATION_BAR_STATUS_CHANGE.equals(intent.getAction()))) {
                    boolean barState = intent.getBooleanExtra("minNavigationBar", false);
                    if (barState) {
                        mListener.hideBottomView();
                    } else {
                        mListener.showBottomView();
                    }
                }
            }
        };
        IntentFilter intent = new IntentFilter();
        intent.addAction(NAVIGATION_BAR_STATUS_CHANGE);
        context.registerReceiver(mNavigationBarBCR, intent);
    }

    public void unregister(Context context) {
        if (mNavigationBarBCR != null) {
            context.unregisterReceiver(mNavigationBarBCR);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static boolean setBothWallpaper(Context context, File file) throws IOException {
        try {
            boolean h = BingWallpaperUtils.setHomeScreenWallpaper(context, file);
            boolean l = BingWallpaperUtils.setLockScreenWallpaper(context, file);
            return h && l;
        } catch (IOException e) {
            return BingWallpaperUtils.setBothWallpaper(context, file);
        }
    }

    public static boolean setWallpaper(Context context, @Constants.setWallpaperMode int mode, File wallpaper)
            throws Exception {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return BingWallpaperUtils.setWallpaper(context, wallpaper);
        } else {
            if (mode == Constants.EXTRA_SET_WALLPAPER_MODE_HOME) {
                return BingWallpaperUtils.setHomeScreenWallpaper(context, wallpaper);
            } else if (mode == Constants.EXTRA_SET_WALLPAPER_MODE_LOCK) {
                return BingWallpaperUtils.setLockScreenWallpaper(context, wallpaper);
            } else {
                return setBothWallpaper(context, wallpaper);
            }
        }
    }

}
