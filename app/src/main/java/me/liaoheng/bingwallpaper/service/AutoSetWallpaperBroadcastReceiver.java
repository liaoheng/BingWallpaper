package me.liaoheng.bingwallpaper.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.NetworkUtils;

import org.joda.time.LocalTime;

import me.liaoheng.bingwallpaper.util.BingWallpaperAlarmManager;
import me.liaoheng.bingwallpaper.util.LogDebugFileUtils;
import me.liaoheng.bingwallpaper.util.BingWallpaperUtils;

/**
 * 接收定时闹钟与开机自启事件
 * @author liaoheng
 * @version 2016-09-19 15:49
 */
public class AutoSetWallpaperBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        L.Log.i("AutoSetWallpaperBroadcastReceiver", "action : %s", intent.getAction());
        if (BingWallpaperUtils.isEnableLog(context)) {
            LogDebugFileUtils.get()
                    .i("AutoSetWallpaperBroadcastReceiver", "action  : %s", intent.getAction());
        }
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            LocalTime dayUpdateTime = BingWallpaperUtils.getDayUpdateTime(context);
            if (dayUpdateTime == null) {
                return;
            }
            BingWallpaperAlarmManager.add(context, dayUpdateTime);
            return;
        }

        if (NetworkUtils.isConnectedOrConnecting(context)) {
            if (BingWallpaperUtils.getOnlyWifi(context)) {
                if (!NetworkUtils.isWifiConnected(context)) {
                    return;
                }
            }
            context.startService(new Intent(context, BingWallpaperIntentService.class));
        }
    }
}
