package me.liaoheng.wallpaper.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.request.target.Target;
import com.github.liaoheng.common.util.Callback;
import com.github.liaoheng.common.util.L;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import me.liaoheng.wallpaper.data.BingWallpaperNetworkClient;
import me.liaoheng.wallpaper.model.BingWallpaperImage;
import me.liaoheng.wallpaper.model.BingWallpaperState;
import me.liaoheng.wallpaper.model.Config;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.CrashReportHandle;
import me.liaoheng.wallpaper.util.GlideApp;
import me.liaoheng.wallpaper.util.IUIHelper;
import me.liaoheng.wallpaper.util.LogDebugFileUtils;
import me.liaoheng.wallpaper.util.NotificationUtils;
import me.liaoheng.wallpaper.util.SetWallpaperStateBroadcastReceiverHelper;
import me.liaoheng.wallpaper.util.TasksUtils;
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

    public static void start(Context context, @Nullable BingWallpaperImage image,
            @NonNull Config config) {
        Intent intent = new Intent(context, BingWallpaperIntentService.class);
        intent.putExtra(EXTRA_SET_WALLPAPER_IMAGE, image);
        intent.putExtra(EXTRA_SET_WALLPAPER_CONFIG, config);
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
        BingWallpaperImage image = intent.getParcelableExtra(EXTRA_SET_WALLPAPER_IMAGE);
        Config config = intent.getParcelableExtra(EXTRA_SET_WALLPAPER_CONFIG);
        if (config == null) {
            return;
        }
        L.alog().d(TAG, config.toString());

        if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
            LogDebugFileUtils.get().i(TAG, "Starting > %s", config);
        }

        sendSetWallpaperBroadcast(BingWallpaperState.BEGIN);

        Callback<BingWallpaperImage> callback = new Callback.EmptyCallback<BingWallpaperImage>() {
            @Override
            public void onSuccess(BingWallpaperImage bingWallpaperImage) {
                success(config, bingWallpaperImage);
            }

            @Override
            public void onError(Throwable e) {
                failure(config, e);
            }
        };
        if (image == null) {
            try {
                if (BingWallpaperUtils.isPixabaySupport(getApplicationContext())) {
                    image = BingWallpaperNetworkClient.getPixabaysExecute();
                    image.setImageUrl(image.getUrl());
                } else {
                    image = BingWallpaperNetworkClient.getBingWallpaperSingleCall(getApplicationContext());
                    image.setImageUrl(BingWallpaperUtils.getResolutionImageUrl(getApplicationContext(),
                            image));
                }
            } catch (IOException e) {
                callback.onError(e);
                return;
            }
        } else {
            if (TextUtils.isEmpty(image.getImageUrl())) {
                image.setImageUrl(BingWallpaperUtils.getResolutionImageUrl(getApplicationContext(),
                        image));
            }
        }

        if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
            LogDebugFileUtils.get().i(TAG, "imageUrl : %s", image.getImageUrl());
        }

        try {
            downloadAndSetWallpaper(image, config);
            callback.onSuccess(image);
        } catch (Throwable e) {
            callback.onError(e);
        }
    }

    private void failure(Config config, Throwable throwable) {
        L.alog().e(TAG, throwable, "Failure");
        if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
            LogDebugFileUtils.get().e(TAG, throwable, "Failure");
        }
        sendSetWallpaperBroadcast(BingWallpaperState.FAIL);
        CrashReportHandle.collectException(getApplicationContext(), TAG, throwable);
        if (!config.isBackground() && !config.isShowNotification()) {
            return;
        }
        NotificationUtils.showFailureNotification(getApplicationContext());
    }

    private void success(Config config, BingWallpaperImage image) {
        if (config.isBackground()) {
            BingWallpaperUtils.setLastWallpaperImageUrl(getApplicationContext(), image.getImageUrl());
            if (TasksUtils.isToDaysDoProvider(getApplicationContext(), 1, FLAG_SET_WALLPAPER_STATE)) {
                L.alog().i(TAG, "Today markDone");
                if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
                    LogDebugFileUtils.get().i(TAG, "Today markDone");
                }
                TasksUtils.markDoneProvider(getApplicationContext(), FLAG_SET_WALLPAPER_STATE);
            }
            showSuccessNotification(image, BingWallpaperUtils.isAutomaticUpdateNotification(getApplicationContext()));
        } else {
            showSuccessNotification(image, config.isShowNotification());
        }

        L.alog().i(TAG, "Complete");
        if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
            LogDebugFileUtils.get().i(TAG, "Complete");
        }

        AppWidget_5x2.start(this, image);
        AppWidget_5x1.start(this, image);
        sendSetWallpaperBroadcast(BingWallpaperState.SUCCESS);
    }

    private void showSuccessNotification(BingWallpaperImage image, boolean isShow) {
        NotificationUtils.clearFailureNotification(getApplicationContext());
        if (isShow) {
            NotificationUtils.showSuccessNotification(getApplicationContext(), image.getCopyright());
        }
    }

    private void downloadAndSetWallpaper(BingWallpaperImage image, Config config)
            throws Exception {
        String url = image.getImageUrl();
        L.alog().i(TAG, "wallpaper image url: " + url);
        File wallpaper = GlideApp.with(getApplicationContext())
                .asFile()
                .load(url)
                .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .get(2, TimeUnit.MINUTES);

        if (wallpaper == null || !wallpaper.exists()) {
            throw new IOException("Download wallpaper failure");
        }

        mUiHelper.setWallpaper(getApplicationContext(), config.getWallpaperMode(), config, wallpaper);

        L.alog().i(TAG, "setBingWallpaper Success");
        if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
            LogDebugFileUtils.get().i(TAG, "setBingWallpaper Success");
        }
        if (!config.isBackground()) {
            return;
        }
        if (BingWallpaperUtils.isAutoSave(this)) {
            try {
                if (!BingWallpaperUtils.checkStoragePermissions(this)) {
                    throw new IOException("Permission denied");
                }
                String saveResolution = BingWallpaperUtils.getSaveResolution(this);
                String resolution = BingWallpaperUtils.getResolution(this);
                if (!saveResolution.equals(resolution)) {
                    String saveImageUrl = BingWallpaperUtils.getImageUrl(getApplicationContext(), saveResolution,
                            image);
                    wallpaper = GlideApp.with(getApplicationContext())
                            .asFile()
                            .load(saveImageUrl)
                            .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                            .get(2, TimeUnit.MINUTES);
                }
                Uri file = BingWallpaperUtils.saveFileToPictureCompat(this, url, wallpaper);
                if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
                    LogDebugFileUtils.get().i(TAG, "save wallpaper to: %s", file);
                }
            } catch (Exception e) {
                CrashReportHandle.saveWallpaper(getApplicationContext(), TAG, e);
            }
        }
    }

    private void sendSetWallpaperBroadcast(BingWallpaperState state) {
        SetWallpaperStateBroadcastReceiverHelper.sendSetWallpaperBroadcast(this, state);
    }
}
