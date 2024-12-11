package me.liaoheng.wallpaper.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.core.content.ContextCompat;

import me.liaoheng.wallpaper.model.BingWallpaperState;
import me.liaoheng.wallpaper.service.SetWallpaperStateBroadcastReceiver;

/**
 * @author liaoheng
 * @version 2019-04-08 10:48
 */
public class SetWallpaperStateBroadcastReceiverHelper {
    private final BroadcastReceiver mBroadcastReceiver;

    public SetWallpaperStateBroadcastReceiverHelper(Callback4<BingWallpaperState> callback) {
        mBroadcastReceiver = new SetWallpaperStateBroadcastReceiver(callback);
    }

    public void register(Context context) {
        ContextCompat.registerReceiver(context, mBroadcastReceiver,
                new IntentFilter(Constants.ACTION_GET_WALLPAPER_STATE), ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    public void unregister(Context context) {
        context.unregisterReceiver(mBroadcastReceiver);
    }

    public static void sendSetWallpaperBroadcast(Context context, BingWallpaperState state) {
        Intent intent = new Intent(Constants.ACTION_GET_WALLPAPER_STATE);
        intent.putExtra(Constants.EXTRA_GET_WALLPAPER_STATE, state.getState());
        intent.setPackage(context.getPackageName());
        context.sendBroadcast(intent);
    }
}
