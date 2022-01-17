package me.liaoheng.wallpaper.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import com.github.liaoheng.common.util.AppUtils;

import java.io.IOException;

import me.liaoheng.wallpaper.model.WallpaperImage;

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

    public static void setWallpaper(Context context, @Constants.setWallpaperMode int mode, WallpaperImage image)
            throws IOException {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            AppUtils.setWallpaper(context, image.getHome());
        } else {
            if (mode == Constants.EXTRA_SET_WALLPAPER_MODE_HOME) {
                AppUtils.setHomeScreenWallpaper(context, image.getHome());
            } else if (mode == Constants.EXTRA_SET_WALLPAPER_MODE_LOCK) {
                AppUtils.setLockScreenWallpaper(context, image.getLock());
            } else {
                AppUtils.setHomeScreenWallpaper(context, image.getHome());
                AppUtils.setLockScreenWallpaper(context, image.getLock());
            }
        }
    }

}
