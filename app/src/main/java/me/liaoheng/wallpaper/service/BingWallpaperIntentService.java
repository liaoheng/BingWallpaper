package me.liaoheng.wallpaper.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.request.target.Target;
import com.github.liaoheng.common.util.Callback;
import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.NetException;
import com.github.liaoheng.common.util.SystemException;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import me.liaoheng.wallpaper.data.BingWallpaperNetworkClient;
import me.liaoheng.wallpaper.model.BingWallpaperImage;
import me.liaoheng.wallpaper.model.BingWallpaperState;
import me.liaoheng.wallpaper.model.Config;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.Constants;
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
    public final static String EXTRA_SET_WALLPAPER_BACKGROUND = "set_wallpaper_background";
    public final static String EXTRA_SET_WALLPAPER_IMAGE = "set_wallpaper_image";
    public final static String EXTRA_SET_WALLPAPER_CONFIG = "set_wallpaper_config";
    private IUIHelper mUiHelper;

    public BingWallpaperIntentService() {
        super("BingWallpaperIntentService");
    }

    /**
     * @param mode 0. both , 1. home , 2. lock
     */
    public static void start(Context context, int mode) {
        start(context, mode, true);
    }

    /**
     * @param mode 0. both , 1. home , 2. lock
     */
    public static void start(Context context, @Constants.setWallpaperMode int mode, boolean background) {
        start(context, null, mode, new Config(context), background);
    }

    /**
     * @param mode 0. both , 1. home , 2. lock
     */
    public static void start(Context context, @Nullable BingWallpaperImage image, @Constants.setWallpaperMode int mode,
            Config config, boolean background) {
        Intent intent = new Intent(context, BingWallpaperIntentService.class);
        intent.putExtra(EXTRA_SET_WALLPAPER_MODE, mode);
        intent.putExtra(EXTRA_SET_WALLPAPER_BACKGROUND, background);
        intent.putExtra(EXTRA_SET_WALLPAPER_IMAGE, image);
        intent.putExtra(EXTRA_SET_WALLPAPER_CONFIG, config);
        ContextCompat.startForegroundService(context, intent);
    }

    @Override
    public void onCreate() {
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
        int setWallpaperType = intent.getIntExtra(EXTRA_SET_WALLPAPER_MODE, 0);
        boolean isBackground = intent.getBooleanExtra(EXTRA_SET_WALLPAPER_BACKGROUND, false);
        BingWallpaperImage bingWallpaperImage = intent.getParcelableExtra(EXTRA_SET_WALLPAPER_IMAGE);
        Config config = intent.getParcelableExtra(EXTRA_SET_WALLPAPER_CONFIG);
        L.alog().d(TAG, " setWallpaperType : %s , config : %s", setWallpaperType, config);

        if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
            LogDebugFileUtils.get().i(TAG, "Starting type: %s , background: %s", setWallpaperType, isBackground);
        }

        sendSetWallpaperBroadcast(BingWallpaperState.BEGIN);

        Callback<BingWallpaperImage> callback = new Callback.EmptyCallback<BingWallpaperImage>() {
            @Override
            public void onSuccess(BingWallpaperImage bingWallpaperImage) {
                success(isBackground, bingWallpaperImage);
            }

            @Override
            public void onError(Throwable e) {
                failure(e);
            }
        };
        String imageUrl;
        if (bingWallpaperImage == null) {
            if (BingWallpaperUtils.isPixabaySupport(getApplicationContext())) {
                try {
                    bingWallpaperImage = BingWallpaperNetworkClient.getPixabaysExecute();
                    imageUrl = bingWallpaperImage.getUrl();
                    bingWallpaperImage.setImageUrl(imageUrl);
                } catch (NetException e) {
                    callback.onError(e);
                    return;
                }
            } else {
                try {
                    String locale = BingWallpaperUtils.getAutoLocale(getApplicationContext());
                    String url = BingWallpaperUtils.getUrl(getApplicationContext());
                    bingWallpaperImage = BingWallpaperNetworkClient.getBingWallpaperSingleCall(url, locale);

                    //To ensure that the latest wallpaper
                    if (BingWallpaperUtils.getLastWallpaperImageUrl(getApplicationContext())
                            .equals(bingWallpaperImage.getUrlbase())) {
                        if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
                            LogDebugFileUtils.get().i(TAG, "check latest wallpaper, skip");
                        }
                        return;
                    }

                    imageUrl = BingWallpaperUtils.getResolutionImageUrl(getApplicationContext(),
                            bingWallpaperImage);
                    bingWallpaperImage.setImageUrl(bingWallpaperImage.getUrlbase());
                } catch (NetException e) {
                    callback.onError(e);
                    return;
                }
            }
        } else {
            imageUrl = bingWallpaperImage.getImageUrl();
        }

        if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
            LogDebugFileUtils.get().i(TAG, "imageUrl : %s", imageUrl);
        }

        try {
            downloadAndSetWallpaper(imageUrl, setWallpaperType, config);
            callback.onSuccess(bingWallpaperImage);
        } catch (Exception e) {
            callback.onError(new SystemException(e));
        }
    }

    private void failure(Throwable throwable) {
        throwable = throwable.getCause() != null ? throwable.getCause() : throwable;
        L.alog().e(TAG, throwable, "Failure");
        if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
            LogDebugFileUtils.get().e(TAG, throwable, "Failure");
        }
        sendSetWallpaperBroadcast(BingWallpaperState.FAIL);
        CrashReportHandle.collectException(getApplicationContext(), TAG, throwable);

        NotificationUtils.showFailureNotification(getApplicationContext());
    }

    private void success(boolean isBackground, BingWallpaperImage bingWallpaperImage) {
        if (isBackground) {
            BingWallpaperUtils.setLastWallpaperImageUrl(getApplicationContext(), bingWallpaperImage.getImageUrl());
            if (TasksUtils.isToDaysDoProvider(getApplicationContext(), 1, FLAG_SET_WALLPAPER_STATE)) {
                L.alog().i(TAG, "Today markDone");
                if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
                    LogDebugFileUtils.get().i(TAG, "Today markDone");
                }
                TasksUtils.markDoneProvider(getApplicationContext(), FLAG_SET_WALLPAPER_STATE);
            }
            if (BingWallpaperUtils.isAutomaticUpdateNotification(getApplicationContext())) {
                NotificationUtils.showSuccessNotification(getApplicationContext(), bingWallpaperImage.getCopyright());
            }
            NotificationUtils.clearFailureNotification(getApplicationContext());
        }
        L.alog().i(TAG, "Complete");
        if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
            LogDebugFileUtils.get().i(TAG, "Complete");
        }

        AppWidget_5x2.start(this, bingWallpaperImage);
        AppWidget_5x1.start(this, bingWallpaperImage);
        sendSetWallpaperBroadcast(BingWallpaperState.SUCCESS);
    }

    private void downloadAndSetWallpaper(String url, @Constants.setWallpaperMode int setWallpaperType, Config config)
            throws Exception {
        L.alog().i(TAG, "wallpaper image url: " + url);
        File wallpaper = GlideApp.with(getApplicationContext())
                .asFile()
                .load(url)
                .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .get(2, TimeUnit.MINUTES);

        if (wallpaper == null || !wallpaper.exists()) {
            throw new IOException("download wallpaper failure");
        }

        if (!mUiHelper.setWallpaper(getApplicationContext(), setWallpaperType, config, wallpaper)) {
            throw new IOException("set wallpaper failure");
        }

        L.alog().i(TAG, "setBingWallpaper Success");
        if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
            LogDebugFileUtils.get().i(TAG, "setBingWallpaper Success");
        }
    }

    private void sendSetWallpaperBroadcast(BingWallpaperState state) {
        SetWallpaperStateBroadcastReceiverHelper.sendSetWallpaperBroadcast(this, state);
    }
}
