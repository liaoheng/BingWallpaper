package me.liaoheng.wallpaper.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.github.liaoheng.common.util.L;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.LogDebugFileUtils;

/**
 * 接收设置壁纸操作
 *
 * @author liaoheng
 * @version 2016-09-19 15:49
 */
public class SetWallpaperBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION = "me.liaoheng.wallpaper.SET_BING_WALLPAPER";
    private final String TAG = SetWallpaperBroadcastReceiver.class.getSimpleName();

    public static void send(Context context, String tag) {
        Intent intent = new Intent(context, SetWallpaperBroadcastReceiver.class);
        intent.setAction(ACTION);
        intent.putExtra("tag", tag);
        context.sendBroadcast(intent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String tag = intent.getStringExtra("tag");
        L.alog().d(TAG, "action : %s  tag: %s", intent.getAction(), tag);
        boolean enableLog = BingWallpaperUtils.isEnableLog(context);
        if (enableLog) {
            LogDebugFileUtils.get()
                    .i(TAG, "action  : %s tag: %s", intent.getAction(), tag);
        }
        if (ACTION.equals(intent.getAction())) {
            int state = BingWallpaperUtils.checkRunningService(context);
            if (state == 1) {
                L.alog().d(TAG, "isConnectedOrConnecting :false");
                if (enableLog) {
                    LogDebugFileUtils.get()
                            .i(TAG, "Network unavailable");
                }
            } else if (state == 2) {
                L.alog().d(TAG, "isWifiConnected :false");
                if (enableLog) {
                    LogDebugFileUtils.get()
                            .i(TAG, "Network not wifi");
                }
            } else if (state == 3) {
                L.alog().d(TAG, "isToDaysDo :false");
                if (enableLog) {
                    LogDebugFileUtils.get()
                            .i(TAG, "Already executed");
                }
            } else if (state == 4) {
                L.alog().d(TAG, "Zero hour skip");
                if (enableLog) {
                    LogDebugFileUtils.get()
                            .i(TAG, "Zero hour skip");
                }
            }
        }
    }
}
