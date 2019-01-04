package me.liaoheng.wallpaper.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import androidx.annotation.RequiresApi;
import com.github.liaoheng.common.util.ROM;

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
    public static void setBothWallpaper(Context context, File bitmap) throws IOException {
        try {
            BingWallpaperUtils.setHomeScreenWallpaper(context, bitmap);
            BingWallpaperUtils.setLockScreenWallpaper(context, bitmap);
        } catch (IOException e) {
            BingWallpaperUtils.setBothWallpaper(context, bitmap);
        }
    }

    public static void setWallpaper(Context context, @Constants.setWallpaperMode int mode, File wallpaper)
            throws Exception {
        if (!ROM.getROM().isEmui()) {
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            BingWallpaperUtils.setWallpaper(context, wallpaper);
        } else {
            if (mode == Constants.EXTRA_SET_WALLPAPER_MODE_HOME) {
                BingWallpaperUtils.setHomeScreenWallpaper(context, wallpaper);
            } else if (mode == Constants.EXTRA_SET_WALLPAPER_MODE_LOCK) {
                BingWallpaperUtils.setLockScreenWallpaper(context, wallpaper);
            } else {
                setBothWallpaper(context, wallpaper);
            }
        }
    }

}
