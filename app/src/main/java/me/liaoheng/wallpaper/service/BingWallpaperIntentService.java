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
import android.util.AndroidRuntimeException;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.github.liaoheng.common.util.AppUtils;
import com.github.liaoheng.common.util.DisplayUtils;
import com.github.liaoheng.common.util.L;

import java.io.File;
import java.util.concurrent.TimeUnit;

import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.data.BingWallpaperNetworkClient;
import me.liaoheng.wallpaper.model.BingWallpaperImage;
import me.liaoheng.wallpaper.model.BingWallpaperState;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.LogDebugFileUtils;
import me.liaoheng.wallpaper.util.TasksUtils;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

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
    //    private FirebaseAnalytics mFirebaseAnalytics;
    /**
     * <p>0. both</p>
     * <p>1. home</p>
     * <p>2. lock</p>
     */
    public final static String EXTRA_SET_WALLPAPER_MODE = "SET_WALLPAPER_MODE";
    public final static String EXTRA_SET_WALLPAPER_BACKGROUND = "SET_WALLPAPER_BACKGROUND";


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
    public static void start(Context context, int mode, boolean background) {
        Intent intent = new Intent(context, BingWallpaperIntentService.class);
        intent.putExtra(EXTRA_SET_WALLPAPER_MODE, mode);
        intent.putExtra(EXTRA_SET_WALLPAPER_BACKGROUND, background);
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
//        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        final int setWallpaperType = intent.getIntExtra(EXTRA_SET_WALLPAPER_MODE, 0);
        final boolean isBackground = intent.getBooleanExtra(EXTRA_SET_WALLPAPER_BACKGROUND, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("bing_wallpaper_intent_service_notification_channel_id", "AutoSetWallpaperIntentService", NotificationManager.IMPORTANCE_LOW);

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager == null) {
                return;
            }
            manager.createNotificationChannel(channel);

            Notification notification = new Notification.Builder(getApplicationContext(), "bing_wallpaper_intent_service_notification_channel_id")
                    .setSmallIcon(R.mipmap.ic_launcher_foreground)
                    .setContentText(getText(R.string.set_wallpaper_running))
                    .setContentTitle(getText(R.string.app_name)).build();

            startForeground(0x111, notification);
        }

        if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
            LogDebugFileUtils.get().i(TAG, "Run BingWallpaperIntentService");
        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (!WallpaperManager.getInstance(getApplicationContext()).isWallpaperSupported()) {
//                Bundle bundle = new Bundle();
//                bundle.putInt("not_supported_wallpaper", 1);
//                mFirebaseAnalytics.logEvent("app_exception", bundle);
//                if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
//                    LogDebugFileUtils.get().i(TAG, "Device not supported wallpaper");
//                }
//                return;
//            }
//        }
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            if (!WallpaperManager.getInstance(getApplicationContext()).isSetWallpaperAllowed()) {
//                Bundle bundle = new Bundle();
//                bundle.putInt("not_allowed_set_wallpaper", 1);
//                mFirebaseAnalytics.logEvent("app_exception", bundle);
//                if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
//                    LogDebugFileUtils.get().i(TAG, "Device not allowed set wallpaper");
//                }
//                return;
//            }
//        }

        sendSetWallpaperBroadcast(BingWallpaperState.BEGIN);

        if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
            LogDebugFileUtils.get().i(TAG, "bing url : %s", BingWallpaperUtils.getUrl());
        }

        BingWallpaperNetworkClient.getBingWallpaper()
                .flatMap(new Func1<BingWallpaperImage, Observable<File>>() {
                    @Override
                    public Observable<File> call(
                            BingWallpaperImage bingWallpaperImage) {
                        String url = BingWallpaperUtils.getUrl(getApplicationContext(), bingWallpaperImage);
                        L.Log.i(TAG, "wallpaper url : %s", url);
                        File wallpaper = null;
                        try {
                            wallpaper = Glide.with(getApplicationContext()).load(url)
                                    .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get(2, TimeUnit.MINUTES);
                            String absolutePath = wallpaper.getAbsolutePath();
                            L.Log.i(TAG, "wallpaper file : " + absolutePath);

                            //切割壁纸
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.outWidth = DisplayUtils
                                    .getScreenWidthRealMetrics(getApplicationContext());
                            options.outHeight = DisplayUtils
                                    .getScreenHeightRealMetrics(getApplicationContext());
                            Bitmap bitmap = BitmapFactory.decodeFile(absolutePath, options);

                            if (setWallpaperType == 1) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    WallpaperManager.getInstance(getApplicationContext())
                                            .setBitmap(bitmap, null, false, WallpaperManager.FLAG_SYSTEM);
                                }
                            } else if (setWallpaperType == 2) {
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
                                if (wallpaper.exists()) {
                                    wallpaper.delete();
                                }
                            }
                        }
                    }
                }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<File>() {
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
