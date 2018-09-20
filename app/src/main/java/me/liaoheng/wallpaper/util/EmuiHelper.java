package me.liaoheng.wallpaper.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.RequiresApi;

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
        boolean bar = BingWallpaperUtils.emuiNavigationEnabled(context);
        if (bar) {
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
    public static void setBothWallpaper(Context context, Bitmap bitmap) throws IOException {
        try {
            BingWallpaperUtils.setHomeScreenWallpaper(context, bitmap);
            BingWallpaperUtils.setLockScreenWallpaper(context, bitmap);
        } catch (IOException e) {
            BingWallpaperUtils.setBothWallpaper(context, bitmap);
        }
    }

}
