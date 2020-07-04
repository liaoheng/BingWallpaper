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
import me.liaoheng.wallpaper.util.SettingUtils;
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

    public void begin(Config config, Wallpaper image) {
        sendSetWallpaperBroadcast(BingWallpaperState.BEGIN);

        L.alog().i(TAG, "set wallpaper url: %s", image.getImageUrl());
        if (config.isBackground()) {
            if (BingWallpaperUtils.isEnableLogProvider(mContext)) {
                LogDebugFileUtils.get().i(TAG, "Set wallpaper url: %s", image.getBaseUrl());
            }
        }
    }

    public void failure(Config config, Throwable throwable) {
        L.alog().e(TAG, throwable, "set wallpaper failure");
        if (BingWallpaperUtils.isEnableLogProvider(mContext)) {
            LogDebugFileUtils.get().e(TAG, throwable, "Set wallpaper failure");
        }
        sendSetWallpaperBroadcast(BingWallpaperState.FAIL);
        CrashReportHandle.collectException(mContext, TAG, throwable);
        if (!config.isBackground() && !config.isShowNotification()) {
            return;
        }
        NotificationUtils.showFailureNotification(mContext);
    }

    public void success(Config config, Wallpaper image) {
        L.alog().i(TAG, "set wallpaper success");
        if (config.isBackground()) {
            if (!SettingUtils.getLastWallpaperImageUrl(mContext).equals(image.getImageUrl())) {
                BingWallpaperUtils.taskComplete(mContext, TAG);
                showSuccessNotification(image, SettingUtils.isAutomaticUpdateNotification(mContext));
                SettingUtils.setLastWallpaperImageUrl(mContext, image.getImageUrl());
            }
        } else {
            showSuccessNotification(image, config.isShowNotification());
        }
        AppWidget_5x2.start(mContext, image);
        AppWidget_5x1.start(mContext, image);
        sendSetWallpaperBroadcast(BingWallpaperState.SUCCESS);
    }

    private void showSuccessNotification(Wallpaper image, boolean isShow) {
        NotificationUtils.clearFailureNotification(mContext);
        if (isShow) {
            NotificationUtils.showSuccessNotification(mContext, image.getCopyright());
        }
    }

    public void sendSetWallpaperBroadcast(BingWallpaperState state) {
        SetWallpaperStateBroadcastReceiverHelper.sendSetWallpaperBroadcast(mContext, state);
    }

}
