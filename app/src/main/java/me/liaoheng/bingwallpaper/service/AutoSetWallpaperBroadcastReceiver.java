package me.liaoheng.bingwallpaper.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.NetworkUtils;
import me.liaoheng.bingwallpaper.util.BingWallpaperAlarmManager;
import me.liaoheng.bingwallpaper.util.LogDebugFileUtils;
import me.liaoheng.bingwallpaper.util.Utils;
import org.joda.time.LocalTime;

/**
 * @author liaoheng
 * @version 2016-09-19 15:49
 */
public class AutoSetWallpaperBroadcastReceiver extends BroadcastReceiver {

    @Override public void onReceive(Context context, Intent intent) {
        boolean enableDayUpdate = Utils.isEnableDayUpdate(context);
        if (!enableDayUpdate) {
            BingWallpaperAlarmManager.clear(context);
            return;
        }
        L.Log.i("AutoSetWallpaperBroadcastReceiver", "action : %s", intent.getAction());
        if (Utils.isEnableLog(context)) {
            LogDebugFileUtils.get()
                    .i("AutoSetWallpaperBroadcastReceiver", "action  : %s", intent.getAction());
        }
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            LocalTime dayUpdateTime = Utils.getDayUpdateTime(context);
            BingWallpaperAlarmManager.clear(context);
            BingWallpaperAlarmManager.add(context,dayUpdateTime);
            return;
        }

        if (NetworkUtils.isConnected(context)) {
            if (Utils.getOnlyWifi(context)) {
                if (!NetworkUtils.isWifiAvailable(context)) {
                    return;
                }
            }
            context.startService(new Intent(context, BingWallpaperIntentService.class));
        }
    }
}
