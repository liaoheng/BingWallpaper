package me.liaoheng.wallpaper.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.request.target.Target;
import com.github.liaoheng.common.util.Callback;
import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.NetException;
import com.github.liaoheng.common.util.SystemException;

import org.apache.commons.io.FilenameUtils;

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
        int setWallpaperType = intent.getIntExtra(EXTRA_SET_WALLPAPER_MODE, 0);
        boolean isBackground = intent.getBooleanExtra(EXTRA_SET_WALLPAPER_BACKGROUND, false);
        BingWallpaperImage image = intent.getParcelableExtra(EXTRA_SET_WALLPAPER_IMAGE);
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
                failure(isBackground, e);
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
            } catch (NetException e) {
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
            downloadAndSetWallpaper(isBackground, image.getImageUrl(), setWallpaperType, config);
            callback.onSuccess(image);
        } catch (Exception e) {
            callback.onError(new SystemException(e));
        }
    }

    private void failure(boolean isBackground, Throwable throwable) {
        throwable = throwable.getCause() != null ? throwable.getCause() : throwable;
        L.alog().e(TAG, throwable, "Failure");
        if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
            LogDebugFileUtils.get().e(TAG, throwable, "Failure");
        }
        sendSetWallpaperBroadcast(BingWallpaperState.FAIL);
        CrashReportHandle.collectException(getApplicationContext(), TAG, throwable);
        if (isBackground) {
            NotificationUtils.showFailureNotification(getApplicationContext());
        }
    }

    private void success(boolean isBackground, BingWallpaperImage image) {
        if (isBackground) {
            BingWallpaperUtils.setLastWallpaperImageUrl(getApplicationContext(), image.getImageUrl());
            if (TasksUtils.isToDaysDoProvider(getApplicationContext(), 1, FLAG_SET_WALLPAPER_STATE)) {
                L.alog().i(TAG, "Today markDone");
                if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
                    LogDebugFileUtils.get().i(TAG, "Today markDone");
                }
                TasksUtils.markDoneProvider(getApplicationContext(), FLAG_SET_WALLPAPER_STATE);
            }
            if (BingWallpaperUtils.isAutomaticUpdateNotification(getApplicationContext())) {
                NotificationUtils.showSuccessNotification(getApplicationContext(), image.getCopyright());
            }
            NotificationUtils.clearFailureNotification(getApplicationContext());
        }
        L.alog().i(TAG, "Complete");
        if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
            LogDebugFileUtils.get().i(TAG, "Complete");
        }

        AppWidget_5x2.start(this, image);
        AppWidget_5x1.start(this, image);
        sendSetWallpaperBroadcast(BingWallpaperState.SUCCESS);
    }

    private void downloadAndSetWallpaper(boolean isBackground, String url,
            @Constants.setWallpaperMode int setWallpaperType, Config config)
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

        mUiHelper.setWallpaper(getApplicationContext(), setWallpaperType, config, wallpaper);

        L.alog().i(TAG, "setBingWallpaper Success");
        if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
            LogDebugFileUtils.get().i(TAG, "setBingWallpaper Success");
        }
        if (!isBackground) {
            return;
        }
        try {
            if (BingWallpaperUtils.isAutoSave(this)) {
                String name = FilenameUtils.getName(url);
                File file = BingWallpaperUtils.saveFileToPicture(this, name, wallpaper);
                if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
                    LogDebugFileUtils.get().i(TAG, "save wallpaper to: %s", file.getAbsoluteFile());
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void sendSetWallpaperBroadcast(BingWallpaperState state) {
        SetWallpaperStateBroadcastReceiverHelper.sendSetWallpaperBroadcast(this, state);
    }
}
