package me.liaoheng.wallpaper.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.github.liaoheng.common.util.L;

import me.liaoheng.wallpaper.util.BingWallpaperJobManager;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.LogDebugFileUtils;
import me.liaoheng.wallpaper.util.Settings;
import me.liaoheng.wallpaper.widget.AppWidget_5x1;
import me.liaoheng.wallpaper.widget.AppWidget_5x2;

/**
 * 接收定时闹钟与开机自启事件
 *
 * @author liaoheng
 * @version 2016-09-19 15:49
 */
public class AutoSetWallpaperBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION = "me.liaoheng.wallpaper.ALARM_TASK_SCHEDULE";
    private final String TAG = AutoSetWallpaperBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            AppWidget_5x1.start(context, null);
            AppWidget_5x2.start(context, null);

            if (Settings.getJobType(context) == Settings.TIMER) {
                BingWallpaperJobManager.enableTimer(context);
            }
            return;
        }
        L.alog().d(TAG, "timer : %s", intent.getAction());
        if (Settings.isEnableLog(context)) {
            LogDebugFileUtils.get()
                    .i(TAG, "timer : %s", intent.getAction());
        }
        if (ACTION.equals(intent.getAction())) {
            BingWallpaperUtils.checkStartSetWallpaper(context, TAG);
        }
    }
}
