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

import me.liaoheng.wallpaper.data.BingWallpaperNetworkClient;
import me.liaoheng.wallpaper.model.Config;
import me.liaoheng.wallpaper.model.Wallpaper;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.IUIHelper;
import me.liaoheng.wallpaper.util.LogDebugFileUtils;
import me.liaoheng.wallpaper.util.NotificationUtils;
import me.liaoheng.wallpaper.util.UIHelper;
import me.liaoheng.wallpaper.util.WallpaperUtils;

/**
 * 设置壁纸操作IntentService
 *
 * @author liaoheng
 * @version 2016-9-19 12:48
 */
public class BingWallpaperIntentService extends IntentService {

    private final String TAG = BingWallpaperIntentService.class.getSimpleName();
    private IUIHelper mUiHelper;
    private SetWallpaperServiceHelper mServiceHelper;

    public BingWallpaperIntentService() {
        super("BingWallpaperIntentService");
    }

    public static void start(Context context, @Nullable Wallpaper image,
            @NonNull Config config) {
        Intent intent = new Intent(context, BingWallpaperIntentService.class);
        intent.putExtra(Config.EXTRA_SET_WALLPAPER_IMAGE, image);
        intent.putExtra(Config.EXTRA_SET_WALLPAPER_CONFIG, config);
        start(context, intent);
    }

    public static void start(Context context, Intent intent) {
        ContextCompat.startForegroundService(context, intent);
    }

    @Override
    public void onCreate() {
        setIntentRedelivery(true);
        mUiHelper = new UIHelper();
        mServiceHelper = new SetWallpaperServiceHelper(this, TAG);
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
        Wallpaper image = intent.getParcelableExtra(Config.EXTRA_SET_WALLPAPER_IMAGE);
        Config config = intent.getParcelableExtra(Config.EXTRA_SET_WALLPAPER_CONFIG);
        if (config == null) {
            return;
        }
        L.alog().d(TAG, config.toString());

        if (BingWallpaperUtils.isEnableLogProvider(this)) {
            LogDebugFileUtils.get().i(TAG, "Starting > %s", config);
        }

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
                image = BingWallpaperNetworkClient.getWallpaper(this, false);
            } catch (IOException e) {
                callback.onError(e);
                return;
            }
        } else {
            if (TextUtils.isEmpty(image.getImageUrl())) {
                image.setResolutionImageUrl(this);
            }
        }

        mServiceHelper.begin(config, image);

        try {
            downloadAndSetWallpaper(image, config);
            callback.onSuccess(image);
        } catch (Throwable e) {
            callback.onError(e);
        }
    }

    private void failure(Config config, Throwable throwable) {
        mServiceHelper.failure(config, throwable);
    }

    private void success(Config config, Wallpaper image) {
        mServiceHelper.success(config, image);
    }

    private void downloadAndSetWallpaper(Wallpaper image, Config config)
            throws Throwable {
        File wallpaper = WallpaperUtils.getImageFile(this, image.getImageUrl());

        if (wallpaper == null || !wallpaper.exists()) {
            throw new IOException("Download wallpaper failure");
        }

        if (config.isBackground()) {
            if (BingWallpaperUtils.getLastWallpaperImageUrl(this).equals(image.getImageUrl())) {
                return;
            }
            WallpaperUtils.autoSaveWallpaper(this, TAG, image, wallpaper);
        }
        mUiHelper.setWallpaper(this, config, wallpaper);
        BingWallpaperUtils.setLastWallpaperImageUrl(this, image.getImageUrl());
    }
}
