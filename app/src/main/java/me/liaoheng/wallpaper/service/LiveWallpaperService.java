package me.liaoheng.wallpaper.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.service.wallpaper.WallpaperService;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;

import com.github.liaoheng.common.util.AppUtils;
import com.github.liaoheng.common.util.Callback;
import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.ROM;
import com.github.liaoheng.common.util.Utils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.data.BingWallpaperNetworkClient;
import me.liaoheng.wallpaper.model.Config;
import me.liaoheng.wallpaper.model.Wallpaper;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.Constants;
import me.liaoheng.wallpaper.util.LogDebugFileUtils;
import me.liaoheng.wallpaper.util.MiuiHelper;
import me.liaoheng.wallpaper.util.Settings;
import me.liaoheng.wallpaper.util.WallpaperUtils;

/**
 * @author liaoheng
 * @version 2020-06-17 16:59
 */
public class LiveWallpaperService extends WallpaperService {
    private final String TAG = LiveWallpaperService.class.getSimpleName();
    public static final String UPDATE_LIVE_WALLPAPER = "me.liaoheng.wallpaper.UPDATE_LIVE_WALLPAPER";
    private LiveWallpaperBroadcastReceiver mReceiver;
    private SetWallpaperServiceHelper mServiceHelper;
    private LiveWallpaperEngine mEngine;

    @Override
    public Engine onCreateEngine() {
        mEngine = new LiveWallpaperEngine();
        return mEngine;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogDebugFileUtils.init(getApplicationContext());
        mServiceHelper = new SetWallpaperServiceHelper(this, TAG);
        mReceiver = new LiveWallpaperBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UPDATE_LIVE_WALLPAPER);
        intentFilter.addAction(Constants.ACTION_DEBUG_LOG);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
        super.onDestroy();
        mEngine = null;
    }

    class LiveWallpaperBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (UPDATE_LIVE_WALLPAPER.equals(intent.getAction())) {
                Wallpaper image = intent.getParcelableExtra(Config.EXTRA_SET_WALLPAPER_IMAGE);
                Config config = intent.getParcelableExtra(Config.EXTRA_SET_WALLPAPER_CONFIG);
                if (mEngine != null) {
                    mEngine.setBingWallpaper(image, config);
                }
            } else if (Constants.ACTION_DEBUG_LOG.equals(intent.getAction())) {
                LogDebugFileUtils.init(getApplicationContext());
            }
        }
    }

    static class DownloadBitmap {
        public DownloadBitmap(Wallpaper image) {
            this.image = image;
        }

        Wallpaper image;
        File wallpaper;
        File original;
    }

    private class LiveWallpaperEngine extends LiveWallpaperService.Engine {
        private Handler handler;
        private HandlerThread mHandlerThread;
        private final Runnable drawRunner;
        private final int width;
        private final int height;

        public LiveWallpaperEngine() {
            DisplayMetrics size = BingWallpaperUtils.getSysResolution(getApplicationContext());
            width = size.widthPixels;
            height = size.heightPixels;
            setOffsetNotificationsEnabled(true);
            mHandlerThread = new HandlerThread(TAG + UUID.randomUUID(), Process.THREAD_PRIORITY_BACKGROUND);
            mHandlerThread.start();
            drawRunner = this::timing;
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mHandlerThread.quit();
            mHandlerThread = null;
        }

        private void postDelayed() {
            if (handler == null) {
                return;
            }
            handler.postDelayed(drawRunner, Constants.DEF_LIVE_WALLPAPER_CHECK_PERIODIC);
        }

        public void setBingWallpaper(Wallpaper image, Config config) {
            Observable<DownloadBitmap> observable;
            if (image == null) {
                observable = Observable.just(true).compose(load());
            } else {
                mServiceHelper.begin(config, image);
                observable = Observable.just(new DownloadBitmap(image));
            }
            setBingWallpaper(observable, config);
        }

        public void setBingWallpaper(Observable<DownloadBitmap> observable, Config config) {
            Utils.addSubscribe(
                    observable.subscribeOn(Schedulers.io()).compose(download(config)),
                    new Callback.EmptyCallback<DownloadBitmap>() {

                        @Override
                        public void onSuccess(DownloadBitmap d) {
                            setWallpaper(config, d);
                            mServiceHelper.success(config, d.image);
                        }

                        @Override
                        public void onError(Throwable e) {
                            mServiceHelper.failure(config, e);
                        }
                    });
        }

        public void enable() {
            destroy();
            handler = new Handler(mHandlerThread.getLooper());
            handler.post(this::timing);
        }

        private void timing() {
            postDelayed();
            L.alog().i(TAG, "timing check...");
            if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
                LogDebugFileUtils.get().i(TAG, "Timing check...");
            }
            if (!BingWallpaperUtils.isTaskUndone(getApplicationContext())) {
                return;
            }
            setBingWallpaper();
        }

        private void setBingWallpaper() {
            Config config = new Config.Builder().setBackground(true)
                    .setShowNotification(true)
                    .loadConfig(getApplicationContext())
                    .setWallpaperMode(Settings.getAutoModeValue(getApplicationContext()))
                    .build();
            setBingWallpaper(Observable.just(false).subscribeOn(Schedulers.io()).compose(load()).map(
                    downloadBitmap -> {
                        mServiceHelper.begin(config, downloadBitmap.image);
                        return downloadBitmap;
                    }).compose(download(config)), config);
        }

        private ObservableTransformer<Boolean, DownloadBitmap> load() {
            return upstream -> upstream.flatMap((Function<Boolean, ObservableSource<DownloadBitmap>>) force -> {
                if (!force) {
                    Intent intent = BingWallpaperUtils.checkRunningServiceIntent(getApplicationContext(), TAG);
                    if (intent == null) {
                        return Observable.empty();
                    }
                }
                try {
                    Wallpaper image = BingWallpaperNetworkClient.getWallpaper(getApplicationContext(), force);
                    return Observable.just(new DownloadBitmap(image));
                } catch (IOException e) {
                    return Observable.error(e);
                }
            });
        }

        private ObservableTransformer<DownloadBitmap, DownloadBitmap> download(Config config) {
            return upstream -> upstream.flatMap((Function<DownloadBitmap, ObservableSource<DownloadBitmap>>) image -> {
                try {
                    File original = WallpaperUtils.getImageFile(getApplicationContext(), image.image.getImageUrl());
                    image.wallpaper = WallpaperUtils.getImageStackBlurFile(config.getStackBlur(), original);
                    image.original = original;
                } catch (Exception e) {
                    return Observable.error(e);
                }
                return Observable.just(image);
            });
        }

        private void setWallpaper(Config config, DownloadBitmap d) {
            if (config.isBackground()) {
                WallpaperUtils.autoSaveWallpaper(getApplicationContext(), TAG, d.image, d.original);
            }
            File home = new File(d.original.toURI());
            File lock = new File(d.original.toURI());
            if (config.getStackBlurMode() == Constants.EXTRA_SET_WALLPAPER_MODE_BOTH) {
                home = d.wallpaper;
                lock = d.wallpaper;
            } else if (config.getStackBlurMode() == Constants.EXTRA_SET_WALLPAPER_MODE_HOME) {
                home = d.wallpaper;
            } else if (config.getStackBlurMode() == Constants.EXTRA_SET_WALLPAPER_MODE_LOCK) {
                lock = d.wallpaper;
            }
            drawWallpaper(home);
            if (config.getWallpaperMode() == Constants.EXTRA_SET_WALLPAPER_MODE_HOME) {
                return;
            }
            //set lock wallpaper
            try {
                if (BingWallpaperUtils.isROMSystem()) {
                    if (ROM.getROM().isMiui()) {
                        MiuiHelper.lockSetWallpaper(getApplicationContext(), lock);
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            AppUtils.setLockScreenWallpaper(getApplicationContext(), lock);
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        private void drawWallpaper(File file) {
            WallpaperUtils.drawSurfaceHolder(getSurfaceHolder(), canvas -> draw(canvas, file));
        }

        private void draw(Canvas canvas, File file) {
            Bitmap bitmap;
            if (file == null || !file.exists()) {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.milky_way);
            } else {
                bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            }
            Bitmap newBitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
            canvas.drawBitmap(newBitmap, 0, 0, null);
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            loadBingWallpaper();
            if (isPreview()) {
                return;
            }
            enable();
        }

        private Disposable mLoadWallpaperDisposable;

        //https://stackoverflow.com/questions/22066481/rxjava-can-i-use-retry-but-with-delay
        public class RetryWithDelay implements Function<Observable<? extends Throwable>, Observable<?>> {
            private final int maxRetries;
            private final int retryDelayMillis;
            private int retryCount;

            public RetryWithDelay(final int maxRetries, final int retryDelayMillis) {
                this.maxRetries = maxRetries;
                this.retryDelayMillis = retryDelayMillis;
                this.retryCount = 0;
            }

            @Override
            public Observable<?> apply(final Observable<? extends Throwable> attempts) {
                return attempts
                        .flatMap((Function<Throwable, Observable<?>>) throwable -> {
                            if (++retryCount < maxRetries) {
                                return Observable.timer(retryDelayMillis,
                                        TimeUnit.SECONDS);
                            }
                            return Observable.error(throwable);
                        });
            }
        }

        private void loadBingWallpaper() {
            Config config = new Config.Builder().loadConfig(getApplicationContext()).build();
            mLoadWallpaperDisposable = Utils.addSubscribe(
                    Observable.just(true)
                            .compose(load())
                            .subscribeOn(Schedulers.io())
                            .compose(download(config)).retryWhen(new RetryWithDelay(6, 5)),
                    new Callback.EmptyCallback<DownloadBitmap>() {

                        @Override
                        public void onSuccess(DownloadBitmap d) {
                            drawWallpaper(d.wallpaper);
                        }

                        @Override
                        public void onError(Throwable e) {
                            mServiceHelper.failure(config, e);
                        }
                    });
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            Utils.dispose(mLoadWallpaperDisposable);
            destroy();
        }

        private void destroy() {
            if (handler != null) {
                handler.removeCallbacks(drawRunner);
                handler = null;
            }
        }
    }
}
