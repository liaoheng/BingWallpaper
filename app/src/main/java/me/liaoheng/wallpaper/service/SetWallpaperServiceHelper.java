package me.liaoheng.wallpaper.service;

import android.content.Context;

import com.github.liaoheng.common.util.L;

import me.liaoheng.wallpaper.model.BingWallpaperState;
import me.liaoheng.wallpaper.model.Config;
import me.liaoheng.wallpaper.model.Wallpaper;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.CrashReportHandle;
import me.liaoheng.wallpaper.util.LogDebugFileUtils;
import me.liaoheng.wallpaper.util.NotificationUtils;
import me.liaoheng.wallpaper.util.SetWallpaperStateBroadcastReceiverHelper;
import me.liaoheng.wallpaper.util.Settings;
import me.liaoheng.wallpaper.widget.AppWidget_5x1;
import me.liaoheng.wallpaper.widget.AppWidget_5x2;

/**
 * @author liaoheng
 * @version 2020-07-01 09:59
 */
public class SetWallpaperServiceHelper {
    private final String TAG;
    private final Context mContext;

    public SetWallpaperServiceHelper(Context context, String tag) {
        mContext = context;
        TAG = tag;
    }

    public void begin(Config config, boolean showNotification) {
        sendSetWallpaperBroadcast(BingWallpaperState.BEGIN);
        L.alog().i(TAG, "start set wallpaper : %s", config);
        if (Settings.isEnableLogProvider(mContext)) {
            LogDebugFileUtils.get().i(TAG, "Start set wallpaper : %s", config);
        }
        if (!showNotification) {
            return;
        }
        if (!config.isShowNotification()) {
            return;
        }
        NotificationUtils.showStartNotification(mContext);
    }

    public void failure(Config config, Throwable throwable) {
        L.alog().e(TAG, throwable, "set wallpaper failure");
        if (Settings.isEnableLogProvider(mContext)) {
            LogDebugFileUtils.get().e(TAG, throwable, "Set wallpaper failure");
        }
        sendSetWallpaperBroadcast(BingWallpaperState.FAIL);
        if (!config.isShowNotification()) {
            return;
        }
        NotificationUtils.clearStartNotification(mContext);
        NotificationUtils.showFailureNotification(mContext);
    }

    public void success(Config config, Wallpaper image) {
        L.alog().i(TAG, "set wallpaper success");
        if (Settings.isEnableLogProvider(mContext)) {
            LogDebugFileUtils.get().i(TAG, "Set wallpaper success");
        }
        if (config.isBackground()) {
            if (!Settings.getLastWallpaperImageUrl(mContext).equals(image.getImageUrl())) {
                BingWallpaperUtils.taskComplete(mContext, TAG);
                showSuccessNotification(image, Settings.isAutomaticUpdateNotification(mContext));
                Settings.setLastWallpaperImageUrl(mContext, image.getImageUrl());
            }
        } else {
            showSuccessNotification(image, config.isShowNotification());
        }
        AppWidget_5x2.start(mContext, image);
        AppWidget_5x1.start(mContext, image);
        sendSetWallpaperBroadcast(BingWallpaperState.SUCCESS);
    }

    private void showSuccessNotification(Wallpaper image, boolean isShow) {
        NotificationUtils.clearStartNotification(mContext);
        NotificationUtils.clearFailureNotification(mContext);
        if (isShow) {
            NotificationUtils.showSuccessNotification(mContext, image.getTitle());
        }
    }

    public void sendSetWallpaperBroadcast(BingWallpaperState state) {
        SetWallpaperStateBroadcastReceiverHelper.sendSetWallpaperBroadcast(mContext, state);
    }

}
