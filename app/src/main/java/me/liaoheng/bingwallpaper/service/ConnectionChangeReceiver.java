package me.liaoheng.bingwallpaper.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.github.liaoheng.common.util.NetworkUtils;
import me.liaoheng.bingwallpaper.util.LogDebugFileUtils;
import me.liaoheng.bingwallpaper.util.TasksUtils;
import me.liaoheng.bingwallpaper.util.Utils;

/**
 * 监听网络状态，加载壁纸
 * @author liaoheng
 * @version 2016-10-13 16:04
 */
public class ConnectionChangeReceiver extends BroadcastReceiver {

    @Override public void onReceive(Context context, Intent intent) {
        if (Utils.isEnableLog(context)) {
            LogDebugFileUtils.get()
                    .i("ConnectionChangeReceiver", "action  : %s", intent.getAction());
        }
        if (NetworkUtils.isConnected(context)) {
            if (Utils.getOnlyWifi(context)) {
                if (!NetworkUtils.isWifiConnected(context)) {
                    return;
                }
            }

            if (TasksUtils.isToDaysDo(1,BingWallpaperIntentService.FLAG_SET_WALLPAPER_STATE)){
                if (Utils.isEnableLog(context)) {
                    LogDebugFileUtils.get()
                            .i("ConnectionChangeReceiver", "Today for the first time");
                }
                context.startService(new Intent(context, BingWallpaperIntentService.class));
            }
        }
    }
}
