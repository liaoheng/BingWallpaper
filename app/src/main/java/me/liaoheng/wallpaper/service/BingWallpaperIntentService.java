package me.liaoheng.wallpaper.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.github.liaoheng.common.util.Callback;
import com.github.liaoheng.common.util.L;

import java.io.File;
import java.io.IOException;

import me.liaoheng.wallpaper.model.Wallpaper;
import me.liaoheng.wallpaper.model.BingWallpaperState;
import me.liaoheng.wallpaper.model.Config;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.CrashReportHandle;
import me.liaoheng.wallpaper.util.IUIHelper;
import me.liaoheng.wallpaper.util.LogDebugFileUtils;
import me.liaoheng.wallpaper.util.NotificationUtils;
import me.liaoheng.wallpaper.util.SetWallpaperStateBroadcastReceiverHelper;
import me.liaoheng.wallpaper.util.UIHelper;
import me.liaoheng.wallpaper.widget.AppWidget_5x1;
import me.liaoheng.wallpaper.widget.AppWidget_5x2;

/**
 * 设置壁纸操作IntentService
 *
 * @author liaoheng
 * @version 2016-9-19 12:48
 */
public class BingWallpaperIntentService extends IntentService {

    private final String TAG = BingWallpaperIntentService.class.getSimpleName();
    public final static String ACTION_GET_WALLPAPER_STATE = "me.liaoheng.wallpaper.BING_WALLPAPER_STATE";
    public final static String EXTRA_GET_WALLPAPER_STATE = "GET_WALLPAPER_STATE";
    public final static String FLAG_SET_WALLPAPER_STATE = "SET_WALLPAPER_STATE";
    /**
     * <p>0. both</p>
     * <p>1. home</p>
     * <p>2. lock</p>
     */
    public final static String EXTRA_SET_WALLPAPER_MODE = "set_wallpaper_mode";
    public final static String EXTRA_SET_WALLPAPER_IMAGE = "set_wallpaper_image";
    public final static String EXTRA_SET_WALLPAPER_CONFIG = "set_wallpaper_config";
    private IUIHelper mUiHelper;

    public BingWallpaperIntentService() {
        super("BingWallpaperIntentService");
    }

    public static void start(Context context, @Nullable Wallpaper image,
            @NonNull Config config) {
        Intent intent = new Intent(context, BingWallpaperIntentService.class);
        intent.putExtra(EXTRA_SET_WALLPAPER_IMAGE, image);
        intent.putExtra(EXTRA_SET_WALLPAPER_CONFIG, config);
        start(context, intent);
    }

    public static void start(Context context, Intent intent) {
        ContextCompat.startForegroundService(context, intent);
    }

    @Override
    public void onCreate() {
        setIntentRedelivery(true);
        mUiHelper = new UIHelper();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        mUiHelper = null;
        stopForeground(true);
        super.onDestroy();
    }

    //https://issuetracker.google.com/issues/76112072
    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        NotificationUtils.showStartNotification(this);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        Wallpaper image = intent.getParcelableExtra(EXTRA_SET_WALLPAPER_IMAGE);
        Config config = intent.getParcelableExtra(EXTRA_SET_WALLPAPER_CONFIG);
        if (config == null) {
            return;
        }
        L.alog().d(TAG, config.toString());

        if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
            LogDebugFileUtils.get().i(TAG, "Starting > %s", config);
        }

        sendSetWallpaperBroadcast(BingWallpaperState.BEGIN);

        Callback<Wallpaper> callback = new Callback.EmptyCallback<Wallpaper>() {
            @Override
            public void onSuccess(Wallpaper bingWallpaperImage) {
                success(config, bingWallpaperImage);
            }

            @Override
            public void onError(Throwable e) {
                failure(config, e);
            }
        };
        if (image == null) {
            try {
                image = BingWallpaperUtils.getImage(getApplicationContext(),false);
            } catch (IOException e) {
                callback.onError(e);
                return;
            }
        } else {
            if (TextUtils.isEmpty(image.getImageUrl())) {
                image.setResolutionImageUrl(getApplicationContext());
            }
        }

        L.alog().i(TAG, "load image url : %s", image.getImageUrl());
        if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
            LogDebugFileUtils.get().i(TAG, "Load image url : %s", image.getImageUrl());
        }

        try {
            downloadAndSetWallpaper(image, config);
            callback.onSuccess(image);
        } catch (Throwable e) {
            callback.onError(e);
        }
    }

    private void failure(Config config, Throwable throwable) {
        L.alog().e(TAG, throwable, "load image failure");
        if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
            LogDebugFileUtils.get().e(TAG, throwable, "Load image failure");
        }
        sendSetWallpaperBroadcast(BingWallpaperState.FAIL);
        CrashReportHandle.collectException(getApplicationContext(), TAG, throwable);
        if (!config.isBackground() && !config.isShowNotification()) {
            return;
        }
        NotificationUtils.showFailureNotification(getApplicationContext());
    }

    private void success(Config config, Wallpaper image) {
        L.alog().i(TAG, "load image success");
        if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
            LogDebugFileUtils.get().i(TAG, "Load image success");
        }

        if (config.isBackground()) {
            BingWallpaperUtils.setLastWallpaperImageUrl(getApplicationContext(), image.getImageUrl());
            BingWallpaperUtils.taskComplete(this, TAG);
            showSuccessNotification(image, BingWallpaperUtils.isAutomaticUpdateNotification(getApplicationContext()));
        } else {
            showSuccessNotification(image, config.isShowNotification());
        }

        AppWidget_5x2.start(this, image);
        AppWidget_5x1.start(this, image);
        sendSetWallpaperBroadcast(BingWallpaperState.SUCCESS);
    }

    private void showSuccessNotification(Wallpaper image, boolean isShow) {
        NotificationUtils.clearFailureNotification(getApplicationContext());
        if (isShow) {
            NotificationUtils.showSuccessNotification(getApplicationContext(), image.getCopyright());
        }
    }

    private void downloadAndSetWallpaper(Wallpaper image, Config config)
            throws Exception {
        String url = image.getImageUrl();
        File wallpaper = BingWallpaperUtils.getGlideFile(getApplicationContext(), url);

        if (wallpaper == null || !wallpaper.exists()) {
            throw new IOException("Download wallpaper failure");
        }

        mUiHelper.setWallpaper(getApplicationContext(), config, wallpaper);

        if (!config.isBackground()) {
            return;
        }
        BingWallpaperUtils.autoSaveWallpaper(this,TAG,image,wallpaper);
    }

    private void sendSetWallpaperBroadcast(BingWallpaperState state) {
        SetWallpaperStateBroadcastReceiverHelper.sendSetWallpaperBroadcast(this, state);
    }
}
