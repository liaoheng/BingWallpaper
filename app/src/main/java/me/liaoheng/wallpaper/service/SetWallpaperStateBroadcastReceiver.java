package me.liaoheng.wallpaper.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import me.liaoheng.wallpaper.model.BingWallpaperState;
import me.liaoheng.wallpaper.util.Callback4;
import me.liaoheng.wallpaper.util.Constants;

/**
 * @author liaoheng
 * @version 2018-02-07 21:13
 */
public class SetWallpaperStateBroadcastReceiver extends BroadcastReceiver {
    private Callback4<BingWallpaperState> mCallback;

    public SetWallpaperStateBroadcastReceiver(
            Callback4<BingWallpaperState> callback) {
        mCallback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Constants.ACTION_GET_WALLPAPER_STATE.equals(intent.getAction())) {
            int extra = intent
                    .getIntExtra(Constants.EXTRA_GET_WALLPAPER_STATE, -1);
            if (extra < 0) {
                return;
            }
            BingWallpaperState state = BingWallpaperState.find(extra);

            if (BingWallpaperState.BEGIN.equals(state)) {
                mCallback.onPreExecute();
            } else if (BingWallpaperState.SUCCESS.equals(state)) {
                mCallback.onYes(state);
                mCallback.onFinish(state);
            } else if (BingWallpaperState.FAIL.equals(state)) {
                mCallback.onNo(state);
                mCallback.onFinish(state);
            }
        }
    }
}
