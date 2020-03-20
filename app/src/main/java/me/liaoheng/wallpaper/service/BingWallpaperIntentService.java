package me.liaoheng.wallpaper.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
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

    private void success(Config config, BingWallpaperImage image) {
        L.alog().i(TAG, "load image success");
        if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
            LogDebugFileUtils.get().i(TAG, "Load image success");
        }

        if (config.isBackground()) {
            BingWallpaperUtils.setLastWallpaperImageUrl(getApplicationContext(), image.getImageUrl());
            if (TasksUtils.isToDaysDoProvider(getApplicationContext(), 1, FLAG_SET_WALLPAPER_STATE)) {
                L.alog().i(TAG, "today complete");
                if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
                    LogDebugFileUtils.get().i(TAG, "Today complete");
                }
                TasksUtils.markDoneProvider(getApplicationContext(), FLAG_SET_WALLPAPER_STATE);
            }
            showSuccessNotification(image, BingWallpaperUtils.isAutomaticUpdateNotification(getApplicationContext()));
        } else {
            showSuccessNotification(image, config.isShowNotification());
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
        File wallpaper = getGlideFile(getApplicationContext(), url);

        if (wallpaper == null || !wallpaper.exists()) {
            throw new IOException("Download wallpaper failure");
        }

        mUiHelper.setWallpaper(getApplicationContext(), config, wallpaper);

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
                    L.alog().i(TAG, "wallpaper save url: " + saveImageUrl);
                    wallpaper = getGlideFile(getApplicationContext(), saveImageUrl);
                }
                BingWallpaperUtils.saveFileToPictureCompat(this, url, wallpaper);
            } catch (Exception e) {
                CrashReportHandle.saveWallpaper(getApplicationContext(), TAG, e);
            }
        }
    }

    private File getGlideFile(Context context, String url) throws Exception {
        return GlideApp.with(context)
                .asFile()
                .load(url)
                .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .get(2, TimeUnit.MINUTES);
    }

    private void sendSetWallpaperBroadcast(BingWallpaperState state) {
        SetWallpaperStateBroadcastReceiverHelper.sendSetWallpaperBroadcast(this, state);
    }
}
