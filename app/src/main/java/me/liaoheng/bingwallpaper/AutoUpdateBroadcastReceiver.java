package me.liaoheng.bingwallpaper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.NetworkUtils;

/**
 * @author liaoheng
 * @version 2016-09-19 15:49
 */
public class AutoUpdateBroadcastReceiver extends BroadcastReceiver {

    @Override public void onReceive(Context context, Intent intent) {
        boolean enableDayUpdate = SettingsUtils.isEnableDayUpdate(context);
        if (!enableDayUpdate) {
            BingWallpaperAlarmManager.clear(context);
            return;
        }
        L.Log.i("AutoUpdateBroadcastReceiver", "AutoUpdateBroadcastReceiver");
        if (NetworkUtils.isConnected(context)) {
            if (SettingsUtils.getOnlyWifi(context)) {
                if (!NetworkUtils.isWifiAvailable(context)) {
                    return;
                }
            }
            context.startService(new Intent(context, BingWallpaperIntentService.class));
        }
    }
}
