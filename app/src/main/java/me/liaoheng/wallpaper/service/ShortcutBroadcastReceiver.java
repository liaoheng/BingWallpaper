package me.liaoheng.wallpaper.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.github.liaoheng.common.util.UIUtils;

import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;

/**
 * @author liaoheng
 * @version 2018-03-20 18:51
 */
public class ShortcutBroadcastReceiver extends BroadcastReceiver {
    public final static String SHORTCUT_SET_WALLPAPER = " me.liaoheng.wallpaper.SHORTCUT_SET_WALLPAPER";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (SHORTCUT_SET_WALLPAPER.equals(intent.getAction())) {
            int mode = intent.getIntExtra(BingWallpaperIntentService.EXTRA_SET_WALLPAPER_MODE, -1);
            if (mode < 0) {
                return;
            }
            if (!BingWallpaperUtils.isConnectedOrConnecting(context)) {
                UIUtils.showToast(context, context.getString(R.string.network_unavailable));
                return;
            }
            BingWallpaperIntentService.start(context, mode, false);
        }
    }
}
