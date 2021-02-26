package me.liaoheng.wallpaper.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Handler;
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

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
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
    public static final String START_LIVE_WALLPAPER_SCHEDULER = "me.liaoheng.wallpaper.START_LIVE_WALLPAPER_SCHEDULER";
    public static final String UPDATE_LIVE_WALLPAPER = "me.liaoheng.wallpaper.UPDATE_LIVE_WALLPAPER";
    private LiveWallpaperBroadcastReceiver mReceiver;
    private SetWallpaperServiceHelper mServiceHelper;
    private LiveWallpaperEngine mEngine;

    @Override
    public Engine onCreateEngine() {
        mEngine = new LiveWallpaperEngine(this);
        return mEngine;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mServiceHelper = new SetWallpaperServiceHelper(this, TAG);
        mReceiver = new LiveWallpaperBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UPDATE_LIVE_WALLPAPER);
        intentFilter.addAction(START_LIVE_WALLPAPER_SCHEDULER);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        mEngine = null;
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
        super.onDestroy();
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
            } else if (START_LIVE_WALLPAPER_SCHEDULER.equals(intent.getAction())) {
                if (mEngine != null) {
                    mEngine.enable();
                }
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
        private Context mContext;
        private Handler handler;
        private Runnable drawRunner;
        private int width;
        private int height;

        public LiveWallpaperEngine(Context context) {
            mContext = context;
            DisplayMetrics size = BingWallpaperUtils.getSysResolution(context);
            width = size.widthPixels;
            height = size.heightPixels;
            setOffsetNotificationsEnabled(true);
            drawRunner = this::timing;
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
            handler = new Handler();
            timing();
        }

        private void timing() {
            postDelayed();
            L.alog().i(TAG, "timing check...");
            if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
                LogDebugFileUtils.get().i(TAG, "Timing check...");
            }
            if (!BingWallpaperUtils.isTaskUndone(mContext)) {
                return;
            }
            setBingWallpaper();
        }

        private void setBingWallpaper() {
            Config config = new Config.Builder().setBackground(true)
                    .setShowNotification(true)
                    .loadConfig(mContext)
                    .setWallpaperMode(Settings.getAutoModeValue(mContext))
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
                    Intent intent = BingWallpaperUtils.checkRunningServiceIntent(mContext, TAG);
                    if (intent == null) {
                        return Observable.empty();
                    }
                }
                try {
                    Wallpaper image = BingWallpaperNetworkClient.getWallpaper(mContext, force);
                    return Observable.just(new DownloadBitmap(image));
                } catch (IOException e) {
                    return Observable.error(e);
                }
            });
        }

        private ObservableTransformer<DownloadBitmap, DownloadBitmap> download(Config config) {
            return upstream -> upstream.flatMap((Function<DownloadBitmap, ObservableSource<DownloadBitmap>>) image -> {
                try {
                    File original = WallpaperUtils.getImageFile(mContext, image.image.getImageUrl());
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
                WallpaperUtils.autoSaveWallpaper(mContext, TAG, d.image, d.original);
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
            draw(home);
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

        private void draw(File file) {
            draw(BitmapFactory.decodeFile(file.getAbsolutePath()));
        }

        private void draw(Bitmap bitmap) {
            SurfaceHolder holder = getSurfaceHolder();
            if (!holder.getSurface().isValid()) {
                return;
            }
            Canvas canvas = null;
            try {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    canvas.drawColor(Color.BLACK);
                    scaleCenterCrop(bitmap, canvas, height, width);
                }
            } catch (Throwable ignored) {
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }

        //https://stackoverflow.com/questions/8112715/how-to-crop-bitmap-center-like-imageview
        public void scaleCenterCrop(Bitmap source, Canvas canvas, int newHeight, int newWidth) {
            Bitmap newBitmap = ThumbnailUtils.extractThumbnail(source, width, height,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
            canvas.drawBitmap(newBitmap, 0, 0, null);

            //int sourceWidth = source.getWidth();
            //int sourceHeight = source.getHeight();
            //
            //float xScale = (float) newWidth / sourceWidth;
            //float yScale = (float) newHeight / sourceHeight;
            //float scale = Math.max(xScale, yScale);
            //
            //float scaledWidth = scale * sourceWidth;
            //float scaledHeight = scale * sourceHeight;
            //
            //float left = (newWidth - scaledWidth) / 2;
            //float top = (newHeight - scaledHeight) / 2;
            //
            //RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);
            //
            //canvas.drawBitmap(source, null, targetRect, null);
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            loadBingWallpaper();
            if (Settings.getJobType(getApplicationContext()) == Settings.LIVE_WALLPAPER) {
                enable();
            }
        }

        private int loadCount;

        private void loadBingWallpaper() {
            Config config = new Config.Builder().loadConfig(mContext).build();
            Utils.addSubscribe(
                    Observable.just(true).compose(load()).subscribeOn(Schedulers.io()).compose(download(config)),
                    new Callback.EmptyCallback<DownloadBitmap>() {

                        @Override
                        public void onSuccess(DownloadBitmap d) {
                            loadCount = 0;
                            draw(d.wallpaper);
                        }

                        @Override
                        public void onError(Throwable e) {
                            loadCount++;
                            if (loadCount > 3) {
                                mServiceHelper.failure(config, e);
                                return;
                            }
                            loadBingWallpaper();
                        }
                    });
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
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
