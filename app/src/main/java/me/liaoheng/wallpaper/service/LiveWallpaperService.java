package me.liaoheng.wallpaper.service;

import android.app.WallpaperColors;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

import androidx.annotation.Nullable;
import androidx.collection.LruCache;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.github.liaoheng.common.util.AppUtils;
import com.github.liaoheng.common.util.BitmapUtils;
import com.github.liaoheng.common.util.Callback;
import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.ROM;
import com.github.liaoheng.common.util.Utils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.ObservableTransformer;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.data.BingWallpaperNetworkClient;
import me.liaoheng.wallpaper.model.Config;
import me.liaoheng.wallpaper.model.Wallpaper;
import me.liaoheng.wallpaper.model.WallpaperImage;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.BitmapCache;
import me.liaoheng.wallpaper.util.Constants;
import me.liaoheng.wallpaper.util.DelayedHandler;
import me.liaoheng.wallpaper.util.HandlerHelper;
import me.liaoheng.wallpaper.util.LogDebugFileUtils;
import me.liaoheng.wallpaper.util.MiuiHelper;
import me.liaoheng.wallpaper.util.RetryWithDelay;
import me.liaoheng.wallpaper.util.Settings;
import me.liaoheng.wallpaper.util.UIHelper;
import me.liaoheng.wallpaper.util.WallpaperUtils;

/**
 * @author liaoheng
 * @version 2020-06-17 16:59
 */
public class LiveWallpaperService extends WallpaperService {
    private final String TAG = LiveWallpaperService.class.getSimpleName();
    public static final String VIEW_LIVE_WALLPAPER = "me.liaoheng.wallpaper.VIEW_LIVE_WALLPAPER";
    public static final String ENABLE_LIVE_WALLPAPER = "me.liaoheng.wallpaper.ENABLE_LIVE_WALLPAPER";
    public static final String EXTRA_ENABLE_LIVE_WALLPAPER = "EXTRA_ENABLE_LIVE_WALLPAPER";
    public static final String UPDATE_LIVE_WALLPAPER = "me.liaoheng.wallpaper.UPDATE_LIVE_WALLPAPER";
    public static final String PERMISSION_UPDATE_LIVE_WALLPAPER = "me.liaoheng.wallpaper.permission.UPDATE_LIVE_WALLPAPER";
    private LiveWallpaperBroadcastReceiver mReceiver;
    private SetWallpaperServiceHelper mServiceHelper;
    private CompositeDisposable mLoadWallpaperDisposable;
    private final long mCheckPeriodic = Constants.DEF_LIVE_WALLPAPER_CHECK_PERIODIC;

    @Override
    public Engine onCreateEngine() {
        return new LiveWallpaperEngine();
    }

    private final class LiveWallpaperBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (UPDATE_LIVE_WALLPAPER.equals(intent.getAction())) {
                Wallpaper image = intent.getParcelableExtra(Config.EXTRA_SET_WALLPAPER_IMAGE);
                Config config = intent.getParcelableExtra(Config.EXTRA_SET_WALLPAPER_CONFIG);
                new Thread(() -> setBingWallpaper(image, config)).start();
            } else if (Constants.ACTION_DEBUG_LOG.equals(intent.getAction())) {
                LogDebugFileUtils.init(getApplicationContext());
            } else if (ENABLE_LIVE_WALLPAPER.equals(intent.getAction())) {
                boolean enable = intent.getBooleanExtra(EXTRA_ENABLE_LIVE_WALLPAPER, false);
                disable();
                if (enable) {
                    enable();
                }
            }
        }
    }

    private ScheduledThreadPoolExecutor mPoolExecutor;

    private final Runnable checkRunnable = this::timing;

    @Override
    public void onCreate() {
        super.onCreate();
        L.alog().d(TAG, "onCreate");
        LogDebugFileUtils.init(this);
        mServiceHelper = new SetWallpaperServiceHelper(this, TAG);
        mPoolExecutor = new ScheduledThreadPoolExecutor(1, r -> {
            Thread thread = new Thread(r);
            thread.setPriority(Thread.MIN_PRIORITY);
            return thread;
        });
        mLoadWallpaperDisposable = new CompositeDisposable();
        mReceiver = new LiveWallpaperBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UPDATE_LIVE_WALLPAPER);
        intentFilter.addAction(Constants.ACTION_DEBUG_LOG);
        intentFilter.addAction(ENABLE_LIVE_WALLPAPER);
        ContextCompat.registerReceiver(this, mReceiver, intentFilter, PERMISSION_UPDATE_LIVE_WALLPAPER,
                new Handler(getMainLooper()), ContextCompat.RECEIVER_NOT_EXPORTED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        L.alog().d(TAG, "onDestroy");
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        }
        Utils.dispose(mLoadWallpaperDisposable);
        disable();
        if (mPoolExecutor != null) {
            mPoolExecutor.shutdown();
        }
        mPoolExecutor = null;
        super.onDestroy();
    }

    private ScheduledFuture<?> mScheduledFuture;

    public void enable() {
        if (mPoolExecutor == null) {
            return;
        }
        disable();
        mScheduledFuture = mPoolExecutor.scheduleWithFixedDelay(checkRunnable, 500, mCheckPeriodic,
                TimeUnit.MILLISECONDS);
    }

    private void disable() {
        if (mScheduledFuture == null) {
            return;
        }
        mScheduledFuture.cancel(true);
    }

    private void timing() {
        L.alog().i(TAG, "timing check...");
        Settings.updateLiveWallpaperHeartbeat(System.currentTimeMillis());
        if (Settings.isEnableLogProvider(this)) {
            LogDebugFileUtils.get().i(TAG, "Timing check...");
        }
        if (!BingWallpaperUtils.isTaskUndone(this)) {
            return;
        }
        Config config = BingWallpaperUtils.checkRunningToConfig(this, TAG);
        if (config == null) {
            return;
        }
        updateBingWallpaper(Observable.just(false).compose(load(config)), config);
    }

    public void setBingWallpaper(Wallpaper image, Config config) {
        if (config == null) {
            config = new Config.Builder().loadConfig(this).build();
        }
        Observable<DownloadBitmap> observable;
        if (image == null) {
            observable = Observable.just(true).compose(load(config));
        } else {
            observable = Observable.just(new DownloadBitmap(image, config));
        }
        updateBingWallpaper(observable, config);
    }

    public void updateBingWallpaper(Observable<DownloadBitmap> observable, Config config) {
        mLoadWallpaperDisposable.add(
                Utils.addSubscribe(observable.subscribeOn(Schedulers.io()).retryWhen(new RetryWithDelay(3, 5)),
                        new Callback.EmptyCallback<DownloadBitmap>() {

                            @Override
                            public void onPreExecute() {
                                mServiceHelper.begin(config, true);
                            }

                            @Override
                            public void onSuccess(DownloadBitmap d) {
                                if (config.isBackground()) {
                                    WallpaperUtils.autoSaveWallpaper(getApplicationContext(), TAG, d.image);
                                }
                                setWallpaper(config, d);
                                mServiceHelper.success(config, d.image);
                            }

                            @Override
                            public void onError(Throwable e) {
                                mServiceHelper.failure(config, e);
                            }
                        }));
    }

    private ObservableTransformer<Boolean, DownloadBitmap> load(Config config) {
        return upstream -> upstream.flatMap((Function<Boolean, ObservableSource<DownloadBitmap>>) force -> {
            try {
                Wallpaper image = BingWallpaperNetworkClient.getWallpaper(this, force);
                return Observable.just(new DownloadBitmap(image, config));
            } catch (IOException e) {
                return Observable.error(e);
            }
        });
    }

    private ObservableTransformer<DownloadBitmap, DownloadBitmap> download() {
        return upstream -> upstream.flatMap((Function<DownloadBitmap, ObservableSource<DownloadBitmap>>) image -> {
            try {
                image.image.setResolutionImageUrl(this);
                File original = WallpaperUtils.getImageFile(this, image.image.getImageUrl());
                image.wallpaper = WallpaperUtils.getWallpaperImage(image.config, original,
                        image.image.getImageUrl());
            } catch (Exception e) {
                return Observable.error(e);
            }
            return Observable.just(image);
        });
    }

    private void setWallpaper(Config config, DownloadBitmap d) {
        Intent intent = new Intent(VIEW_LIVE_WALLPAPER);
        intent.putExtra(Config.EXTRA_SET_WALLPAPER_IMAGE, d.image);
        intent.putExtra(Config.EXTRA_SET_WALLPAPER_CONFIG, d.config);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        if (config.getWallpaperMode() == Constants.EXTRA_SET_WALLPAPER_MODE_HOME) {
            return;
        }
        if (ROM.getROM().isEmui() || UIHelper.isNewMagicUI()) {
            downloadLockWallpaper(d);
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R && ROM.getROM().isMiui()
                && Settings.isMiuiLockScreenSupport(getApplicationContext())) {
            downloadLockWallpaper(d);
        }
    }

    private void downloadLockWallpaper(DownloadBitmap wallpaper) {
        mLoadWallpaperDisposable.add(Utils.addSubscribe(Observable.just(wallpaper)
                .subscribeOn(Schedulers.io())
                .retryWhen(new RetryWithDelay(3, 5))
                .compose(download()), new Callback.EmptyCallback<DownloadBitmap>() {

            @Override
            public void onSuccess(DownloadBitmap d) {
                //set lock wallpaper
                try {
                    if (ROM.getROM().isMiui()) {
                        MiuiHelper.lockSetWallpaper(getApplicationContext(), d.wallpaper);
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            AppUtils.setLockScreenWallpaper(getApplicationContext(), d.wallpaper.getLock());
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }));
    }

    static class DownloadBitmap {
        public DownloadBitmap(Wallpaper image, Config config) {
            this.image = image;
            this.config = config;
        }

        Wallpaper image;
        WallpaperImage wallpaper;
        Config config;
        int width;
        int height;

        private void updateSize(SurfaceHolder holder) {
            width = holder.getSurfaceFrame().width();
            height = holder.getSurfaceFrame().height();
        }

        private String key(SurfaceHolder holder) {
            return key(holder.getSurfaceFrame().width(), holder.getSurfaceFrame().height());
        }

        private String key() {
            return key(width, height);
        }

        private String key(int width, int height) {
            return image.getBaseUrl() + "_" + width + "_" + height + "_" + config.getStackBlurMode() + "_"
                    + config.getStackBlur() + "_" + config.getBrightness() + "_" + config.getBrightnessMode();
        }

        public boolean eq(DownloadBitmap b) {
            if (b == null) {
                return false;
            }
            return key(width, height).equals(key(b.width, b.height));
        }
    }

    private class LiveWallpaperEngine extends LiveWallpaperService.Engine {
        public static final int DOWNLOAD_DRAW = 123;
        public static final int DOWNLOAD_DRAW_DELAY = 400;
        public static final int ENABLE = 456;
        public static final int PREVIEW = 789;
        private DownloadBitmap mLastFile;
        private HandlerHelper mDrawHandlerHelper;
        private Runnable mDrawRunnable;
        private Runnable mSelectWallpaperRunnable;
        private DelayedHandler mActionHandler;
        private Disposable mDisplayDisposable;
        private Disposable mPreviewDisposable;
        private LiveWallpaperEngineBroadcastReceiver mReceiver;
        private LruCache<String, DownloadBitmap> mImageCache;
        private BitmapCache mBitmapCache;
        private String TAG = "LiveWallpaperEngine:";

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            TAG += UUID.randomUUID();
            L.alog().d(TAG, "onCreate");
            setOffsetNotificationsEnabled(true);
            mDrawHandlerHelper = HandlerHelper.create(TAG, Process.THREAD_PRIORITY_DEFAULT, null);
            mActionHandler = new DelayedHandler(Looper.getMainLooper(), msg -> {
                if (msg.what == DOWNLOAD_DRAW) {
                    downloadWallpaper((DownloadBitmap) msg.obj);
                } else if (msg.what == ENABLE) {
                    Intent intent = new Intent(ENABLE_LIVE_WALLPAPER);
                    intent.putExtra(EXTRA_ENABLE_LIVE_WALLPAPER, (boolean) msg.obj);
                    LocalBroadcastManager.getInstance(getDisplayContext()).sendBroadcast(intent);
                } else if (msg.what == PREVIEW) {
                    new Thread(this::previewBingWallpaper).start();
                }
                return true;
            });
            mDrawRunnable = this::drawWallpaper;
            mSelectWallpaperRunnable = this::selectWallpaper;

            mReceiver = new LiveWallpaperEngineBroadcastReceiver();
            LocalBroadcastManager.getInstance(getDisplayContext())
                    .registerReceiver(mReceiver, new IntentFilter(VIEW_LIVE_WALLPAPER));
            mImageCache = new LruCache<>(8);
            mBitmapCache = new BitmapCache();

            if (!isPreview()) {
                mActionHandler.removeMessages(ENABLE);
                mActionHandler.sendDelayed(ENABLE, true, 2000);
            }
        }

        @Override
        public void onDestroy() {
            L.alog().d(TAG, "onDestroy");
            Utils.dispose(mDisplayDisposable);
            Utils.dispose(mPreviewDisposable);
            if (mReceiver != null) {
                LocalBroadcastManager.getInstance(getDisplayContext()).unregisterReceiver(mReceiver);
            }
            if (mActionHandler != null) {
                mActionHandler.removeCallbacksAndMessages(null);
                mActionHandler = null;
            }
            if (mDrawHandlerHelper != null) {
                mDrawHandlerHelper.removeCallbacks(mDrawRunnable);
                mDrawHandlerHelper.release();
                mDrawRunnable = null;
            }
            mImageCache.evictAll();
            mBitmapCache.clear();
            mLastFile = null;
            super.onDestroy();
        }

        private void selectWallpaper() {
            if (mActionHandler == null) {
                return;
            }
            DownloadBitmap image = mImageCache.get(mLastFile.key(getSurfaceHolder()));
            if (image == null) {
                mActionHandler.removeMessages(DOWNLOAD_DRAW);
                mActionHandler.sendDelayed(DOWNLOAD_DRAW, new DownloadBitmap(mLastFile.image, mLastFile.config),
                        DOWNLOAD_DRAW_DELAY);
                return;
            }
            if (image.eq(mLastFile)) {
                return;
            }
            mLastFile = image;
            postDraw();
        }

        private void postDraw() {
            if (mDrawHandlerHelper == null) {
                return;
            }
            mDrawHandlerHelper.removeCallbacks(mDrawRunnable);
            mDrawHandlerHelper.postDelayed(mDrawRunnable, Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? 50 : 10);
        }

        private void drawWallpaper() {
            drawWallpaper(mLastFile);
        }

        private synchronized void drawWallpaper(DownloadBitmap wallpaper) {
            if (getSurfaceHolder() == null) {
                L.alog().e(TAG, "attempt to draw a frame without a valid surface");
                return;
            }
            if (wallpaper == null) {
                return;
            }
            L.alog().d(TAG, "drawWallpaper : %s", wallpaper.key());
            if (wallpaper.wallpaper == null || wallpaper.wallpaper.getHome() == null) {
                return;
            }
            Bitmap bitmap = mBitmapCache.get(wallpaper.key());
            if (wallpaper.width == 0 && wallpaper.height == 0) {
                bitmap = BitmapUtils.drawableToBitmap(
                        getDisplayContext().getResources().getDrawable(R.drawable.background));
            } else if (bitmap == null || bitmap.isRecycled()) {
                if (wallpaper.wallpaper.getHome().exists()) {
                    bitmap = BitmapFactory.decodeFile(wallpaper.wallpaper.getHome().getAbsolutePath());
                    if (bitmap != null) {
                        mBitmapCache.put(wallpaper.key(), bitmap);
                    }
                }
            }
            if (bitmap == null || bitmap.isRecycled()) {
                L.alog().d(TAG, "drawWallpaper recycled");
                return;
            }
            final Bitmap finalBitmap = bitmap;
            WallpaperUtils.drawSurfaceHolder(getSurfaceHolder(), canvas -> {
                draw(canvas, finalBitmap, getSurfaceHolder().getSurfaceFrame().width(),
                        getSurfaceHolder().getSurfaceFrame().height());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 && !isPreview()) {
                    notifyColorsChanged();
                }
            });
        }

        private Paint mBitmapPaint;
        private Matrix mMatrix;
        private PointF mTranslate;
        private PointF mPendingCenter;

        private void draw(Canvas canvas, Bitmap bitmap, int width, int height) {
            if (mBitmapPaint == null) {
                mBitmapPaint = new Paint();
                mBitmapPaint.setAntiAlias(true);
                mBitmapPaint.setFilterBitmap(true);
                mBitmapPaint.setDither(true);
            }
            if (mMatrix == null) {
                mMatrix = new Matrix();
            }
            mMatrix.reset();
            if (mPendingCenter == null) {
                mPendingCenter = new PointF();
            }
            if (mTranslate == null) {
                mTranslate = new PointF();
            }

            float scale = Math.max(width / (float) sWidth(bitmap), height / (float) sHeight(bitmap));

            mPendingCenter.x = sWidth(bitmap) / 2F;
            mPendingCenter.y = sHeight(bitmap) / 2F;

            mTranslate.x = (width / 2F) - (scale * mPendingCenter.x);
            mTranslate.y = (height / 2F) - (scale * mPendingCenter.y);

            mMatrix.setScale(scale, scale);
            mMatrix.postTranslate(mTranslate.x, mTranslate.y);
            canvas.drawBitmap(bitmap, mMatrix, mBitmapPaint);
        }

        private int sWidth(Bitmap bitmap) {
            return bitmap.getWidth();
        }

        private int sHeight(Bitmap bitmap) {
            return bitmap.getHeight();
        }

        @Override
        public Context getDisplayContext() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                return getApplicationContext();
            }
            return super.getDisplayContext();
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            L.alog().d(TAG, "onSurfaceChanged width:%s, height:%s", width, height);
            if (mLastFile == null || mDrawHandlerHelper == null) {
                return;
            }
            mDrawHandlerHelper.removeCallbacks(mSelectWallpaperRunnable);
            mDrawHandlerHelper.postDelayed(mSelectWallpaperRunnable, 50);
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            L.alog().d(TAG, "onSurfaceCreated");
            mActionHandler.sendDelayed(PREVIEW, 1000);
        }

        @Nullable
        @Override
        public WallpaperColors onComputeColors() {
            if (mLastFile == null) {
                return null;
            }
            Bitmap bitmap = mBitmapCache.get(mLastFile.key());
            if (bitmap == null) {
                return null;
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
                return WallpaperColors.fromBitmap(bitmap);
            }
            return null;
        }

        private final class LiveWallpaperEngineBroadcastReceiver extends BroadcastReceiver {

            @Override
            public void onReceive(Context context, Intent intent) {
                if (VIEW_LIVE_WALLPAPER.equals(intent.getAction())) {
                    Wallpaper image = intent.getParcelableExtra(Config.EXTRA_SET_WALLPAPER_IMAGE);
                    Config config = intent.getParcelableExtra(Config.EXTRA_SET_WALLPAPER_CONFIG);
                    DownloadBitmap info = new DownloadBitmap(image, config);
                    if (mActionHandler == null) {
                        return;
                    }
                    mActionHandler.removeMessages(DOWNLOAD_DRAW);
                    mActionHandler.sendDelayed(DOWNLOAD_DRAW, info, DOWNLOAD_DRAW_DELAY);
                }
            }
        }

        private void previewBingWallpaper() {
            Config config = new Config.Builder().loadConfig(getApplicationContext()).build();
            mPreviewDisposable = Utils.addSubscribe(Observable.just(true)
                    .subscribeOn(Schedulers.io())
                    .compose(load(config))
                    .retryWhen(new RetryWithDelay(6, 5)), new Callback.EmptyCallback<DownloadBitmap>() {

                @Override
                public void onSuccess(DownloadBitmap info) {
                    downloadWallpaper(info);
                }

                @Override
                public void onError(Throwable e) {
                    setWallpaperDef();
                    mServiceHelper.failure(config, e);
                }
            });
        }

        private void setWallpaperDef() {
            Config config = new Config.Builder().build();
            Wallpaper wallpaper = new Wallpaper("", "", "", "", "", "", "");
            DownloadBitmap bitmap = new DownloadBitmap(wallpaper, config);
            File file = new File("");
            bitmap.wallpaper = new WallpaperImage("", file, file);
            drawWallpaper(bitmap);
        }

        private void downloadWallpaper(DownloadBitmap wallpaper) {
            Utils.dispose(mDisplayDisposable);
            mDisplayDisposable = Utils.addSubscribe(Observable.just(wallpaper)
                    .subscribeOn(Schedulers.io())
                    .retryWhen(new RetryWithDelay(3, 20))
                    .compose(download()), new Callback.EmptyCallback<DownloadBitmap>() {

                @Override
                public void onSuccess(DownloadBitmap d) {
                    d.updateSize(getSurfaceHolder());
                    mLastFile = d;
                    mImageCache.put(d.key(), d);
                    postDraw();
                }
            });
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            L.alog().d(TAG, "onSurfaceDestroyed");
            mActionHandler.removeMessages(PREVIEW);
            mBitmapPaint = null;
            mMatrix = null;
            mTranslate = null;
            mPendingCenter = null;
        }
    }
}


