package me.liaoheng.wallpaper.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.github.liaoheng.common.util.Callback4;

import me.liaoheng.wallpaper.model.BingWallpaperState;
import me.liaoheng.wallpaper.service.SetWallpaperStateBroadcastReceiver;

/**
 * @author liaoheng
 * @version 2019-04-08 10:48
 */
public class SetWallpaperStateBroadcastReceiverHelper {
    private BroadcastReceiver mBroadcastReceiver;

    public SetWallpaperStateBroadcastReceiverHelper(Callback4<BingWallpaperState> callback) {
        mBroadcastReceiver = new SetWallpaperStateBroadcastReceiver(callback);
    }

    public void register(Context context) {
        context.registerReceiver(mBroadcastReceiver,
                new IntentFilter(Constants.ACTION_GET_WALLPAPER_STATE));
    }

    public void unregister(Context context) {
        if (mBroadcastReceiver != null) {
            context.unregisterReceiver(mBroadcastReceiver);
        }
    }

    public static void sendSetWallpaperBroadcast(Context context, BingWallpaperState state) {
        Intent intent = new Intent(Constants.ACTION_GET_WALLPAPER_STATE);
        intent.putExtra(Constants.EXTRA_GET_WALLPAPER_STATE, state.getState());
        context.sendBroadcast(intent);
    }
}
