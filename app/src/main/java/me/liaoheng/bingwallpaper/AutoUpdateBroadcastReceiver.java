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
        L.Log.i("AutoUpdateBroadcastReceiver", "AutoUpdateBroadcastReceiver");
        if (NetworkUtils.isConnected(context) && NetworkUtils.isWifiConnected(context)){
            Intent intent1 = new Intent(context, BingWallpaperIntentService.class);
            intent1.putExtra(BingWallpaperIntentService.AUTO, true);
            context.startService(intent1);
        }
    }
}
