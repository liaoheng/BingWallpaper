package me.liaoheng.wallpaper.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.github.liaoheng.common.util.AppUtils;
import com.github.liaoheng.common.util.FileUtils;
import com.github.liaoheng.common.util.L;

import java.io.File;
import java.util.concurrent.TimeUnit;

import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.data.BingWallpaperNetworkClient;
import me.liaoheng.wallpaper.model.BingWallpaperImage;
import me.liaoheng.wallpaper.model.BingWallpaperState;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.Constants;
import me.liaoheng.wallpaper.util.LogDebugFileUtils;
import me.liaoheng.wallpaper.util.TasksUtils;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 设置壁纸操作IntentService
 *
 * @author liaoheng
 * @version 2016-9-19 12:48
 */
public class BingWallpaperIntentService extends IntentService {

    private final String TAG = BingWallpaperIntentService.class
            .getSimpleName();
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
    public final static String EXTRA_SET_WALLPAPER_URL = "set_wallpaper_url";

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
        start(context, "", mode, background);
    }

    /**
     * @param mode 0. both , 1. home , 2. lock
     */
    public static void start(Context context, String url, @Constants.setWallpaperMode int mode, boolean background) {
        Intent intent = new Intent(context, BingWallpaperIntentService.class);
        intent.putExtra(EXTRA_SET_WALLPAPER_MODE, mode);
        intent.putExtra(EXTRA_SET_WALLPAPER_BACKGROUND, background);
        intent.putExtra(EXTRA_SET_WALLPAPER_URL, url);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    private void clearNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager == null) {
                return;
            }
            manager.cancelAll();
        }
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        int setWallpaperType = intent.getIntExtra(EXTRA_SET_WALLPAPER_MODE, 0);
        final boolean isBackground = intent.getBooleanExtra(EXTRA_SET_WALLPAPER_BACKGROUND, false);
        String setWallpaperUrl = intent.getStringExtra(EXTRA_SET_WALLPAPER_URL);
        L.Log.d(TAG, " setWallpaperType : " + setWallpaperType);

        if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
            LogDebugFileUtils.get().i(TAG, "Run BingWallpaperIntentService");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "bing_wallpaper_intent_service_notification_channel_id", "AutoSetWallpaperIntentService",
                    NotificationManager.IMPORTANCE_LOW);

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager == null) {
                return;
            }
            manager.createNotificationChannel(channel);

            Notification notification = new Notification.Builder(getApplicationContext(),
                    "bing_wallpaper_intent_service_notification_channel_id")
                    .setSmallIcon(R.mipmap.ic_launcher_foreground)
                    .setContentText(getText(R.string.set_wallpaper_running))
                    .setContentTitle(getText(R.string.app_name)).build();

            startForeground(0x111, notification);
        }

        sendSetWallpaperBroadcast(BingWallpaperState.BEGIN);

        if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
            LogDebugFileUtils.get().i(TAG, "bing url : %s", BingWallpaperUtils.getUrl());
        }
        Observable<String> bingWallpaper;
        if (TextUtils.isEmpty(setWallpaperUrl)) {
            bingWallpaper = BingWallpaperNetworkClient.getBingWallpaperSingle().flatMap(
                    new Func1<BingWallpaperImage, Observable<String>>() {
                        @Override
                        public Observable<String> call(BingWallpaperImage bingWallpaperImage) {
                            return Observable.just(BingWallpaperUtils.getResolutionImageUrl(getApplicationContext(),
                                    bingWallpaperImage));
                        }
                    });
        } else {
            bingWallpaper = Observable.just(setWallpaperUrl).subscribeOn(Schedulers.io());
        }
        bingWallpaper.compose(applyDownload(setWallpaperType))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<File>() {
                    @Override
                    public void call(File file) {
                        L.Log.i(TAG, "setBingWallpaper Success");
                        if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
                            LogDebugFileUtils.get().i(TAG, "setBingWallpaper Success");
                        }
                        sendSetWallpaperBroadcast(BingWallpaperState.SUCCESS);
                        if (isBackground) {
                            //标记成功，每天只在后台执行一次
                            if (TasksUtils.isToDaysDoProvider(getApplicationContext(), 1, FLAG_SET_WALLPAPER_STATE)) {
                                L.Log.i(TAG, "Today markDone");
                                if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
                                    LogDebugFileUtils.get().i(TAG, "Today markDone");
                                }
                                TasksUtils.markDoneProvider(getApplicationContext(), FLAG_SET_WALLPAPER_STATE);
                            }
                        }
                        clearNotification();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        L.Log.e(TAG, throwable, "setBingWallpaper Error");
                        if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
                            LogDebugFileUtils.get().e(TAG, throwable, "setBingWallpaper Error");
                        }
                        sendSetWallpaperBroadcast(BingWallpaperState.FAIL);
                        clearNotification();
                    }
                });

    }

    private Observable.Transformer<String, File> applyDownload(final int setWallpaperType) {
        return new Observable.Transformer<String, File>() {
            @Override
            public Observable<File> call(Observable<String> stringObservable) {
                return stringObservable.flatMap(new Func1<String, Observable<File>>() {
                    @Override
                    public Observable<File> call(String url) {
                        L.Log.i(TAG, "wallpaper image url: " + url);
                        File wallpaper = null;
                        try {
                            wallpaper = Glide.with(getApplicationContext()).load(url)
                                    .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get(2, TimeUnit.MINUTES);
                            String absolutePath = wallpaper.getAbsolutePath();
                            L.Log.i(TAG, "wallpaper file : " + absolutePath);
                            Bitmap bitmap = BitmapFactory.decodeFile(absolutePath);

                            if (setWallpaperType == Constants.EXTRA_SET_WALLPAPER_MODE_HOME) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    WallpaperManager.getInstance(getApplicationContext())
                                            .setBitmap(bitmap, null, false, WallpaperManager.FLAG_SYSTEM);
                                }
                            } else if (setWallpaperType == Constants.EXTRA_SET_WALLPAPER_MODE_LOCK) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    WallpaperManager.getInstance(getApplicationContext())
                                            .setBitmap(bitmap, null, false, WallpaperManager.FLAG_LOCK);
                                }
                            } else {
                                WallpaperManager.getInstance(getApplicationContext())
                                        .setBitmap(bitmap);
                            }

                            if (bitmap != null) {
                                if (!bitmap.isRecycled()) {
                                    bitmap.recycle();
                                }
                            }
                            return Observable.just(wallpaper);
                        } catch (Exception e) {
                            throw new AndroidRuntimeException(e);
                        } finally {
                            if (wallpaper != null) {
                                FileUtils.delete(wallpaper);
                            }
                        }
                    }
                });
            }
        };
    }

    private void sendSetWallpaperBroadcast(BingWallpaperState state) {
        if (BingWallpaperState.FAIL.equals(state)) {
            if (!AppUtils.isForeground(getApplicationContext())) {
                Toast.makeText(getApplicationContext(), R.string.set_wallpaper_failure, Toast.LENGTH_LONG).show();
            }
        }
        Intent intent = new Intent(ACTION_GET_WALLPAPER_STATE);
        intent.putExtra(EXTRA_GET_WALLPAPER_STATE, state.getState());
        sendBroadcast(intent);
    }
}
