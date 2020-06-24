package me.liaoheng.wallpaper.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;

import com.github.liaoheng.common.util.BitmapUtils;
import com.github.liaoheng.common.util.Callback;
import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.MD5Utils;
import com.github.liaoheng.common.util.Utils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import me.liaoheng.wallpaper.model.BingWallpaperState;
import me.liaoheng.wallpaper.model.Wallpaper;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.CacheUtils;
import me.liaoheng.wallpaper.util.LogDebugFileUtils;
import me.liaoheng.wallpaper.util.SetWallpaperStateBroadcastReceiverHelper;

/**
 * @author liaoheng
 * @version 2020-06-17 16:59
 */
public class LiveWallpaperService extends WallpaperService {
    private String TAG = LiveWallpaperService.class.getSimpleName();
    public static final String UPDATE_LIVE_WALLPAPER = "me.liaoheng.wallpaper.UPDATE_LIVE_WALLPAPER";
    private LiveWallpaperBroadcastReceiver mReceiver;
    private LiveWallpaperEngine mEngine;

    @Override
    public Engine onCreateEngine() {
        mEngine = new LiveWallpaperEngine(this);
        return mEngine;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mReceiver = new LiveWallpaperBroadcastReceiver();
        registerReceiver(mReceiver, new IntentFilter(UPDATE_LIVE_WALLPAPER));
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
                Wallpaper image = intent.getParcelableExtra("image");
                if (mEngine != null) {
                    mEngine.loadBingWallpaper(image);
                }
            }
        }
    }

    static class DownloadBitmap {
        public DownloadBitmap(Wallpaper image) {
            this.image = image;
        }

        Wallpaper image;
        Bitmap bitmap;
    }

    private class LiveWallpaperEngine extends LiveWallpaperService.Engine {
        private Context mContext;
        private Handler handler;
        private Runnable drawRunner;
        private Disposable mDisposable;
        private int width;
        private int height;

        public LiveWallpaperEngine(Context context) {
            mContext = context;
            handler = new Handler();
            DisplayMetrics size = BingWallpaperUtils.getSysResolution(context);
            width = size.widthPixels;
            height = size.heightPixels;
            setOffsetNotificationsEnabled(true);
            drawRunner = this::timing;
        }

        private void postDelayed() {
            handler.postDelayed(drawRunner,
                    TimeUnit.HOURS.toMillis(BingWallpaperUtils.getAutomaticUpdateInterval(mContext)));
            //handler.postDelayed(drawRunner, TimeUnit.MINUTES.toMillis(10));
        }

        public void loadBingWallpaper(Wallpaper image) {
            handler.removeCallbacks(drawRunner);
            postDelayed();
            mDisposable = Utils.addSubscribe(
                    Observable.just(new DownloadBitmap(image)).subscribeOn(Schedulers.io()).compose(download()),
                    new Callback.EmptyCallback<DownloadBitmap>() {

                        @Override
                        public void onSuccess(DownloadBitmap d) {
                            draw(d.bitmap);
                            SetWallpaperStateBroadcastReceiverHelper.sendSetWallpaperBroadcast(mContext,
                                    BingWallpaperState.SUCCESS);
                            BingWallpaperUtils.setLastWallpaperImageUrl(getApplicationContext(), d.image.getImageUrl());
                        }

                        @Override
                        public void onError(Throwable e) {
                            SetWallpaperStateBroadcastReceiverHelper.sendSetWallpaperBroadcast(mContext,
                                    BingWallpaperState.FAIL);
                        }
                    });
        }

        public void init() {
            L.alog().d(TAG, "init");
            handler.removeCallbacks(drawRunner);
            setBingWallpaper(true);
            postDelayed();
        }

        private void timing() {
            postDelayed();
            L.alog().d(TAG, "timing check...");
            if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
                LogDebugFileUtils.get().i(TAG, "Timing check...");
            }
            if (!BingWallpaperUtils.isTaskUndone(mContext)) {
                return;
            }
            setBingWallpaper(false);
        }

        private void setBingWallpaper(boolean init) {
            mDisposable = Utils.addSubscribe(
                    Observable.just(init).subscribeOn(Schedulers.io()).compose(load()).compose(download()),
                    new Callback.EmptyCallback<DownloadBitmap>() {

                        @Override
                        public void onSuccess(DownloadBitmap d) {
                            draw(d.bitmap);
                            BingWallpaperUtils.setLastWallpaperImageUrl(getApplicationContext(), d.image.getImageUrl());
                            BingWallpaperUtils.taskComplete(mContext, TAG);
                        }
                    });
        }

        private ObservableTransformer<Boolean, DownloadBitmap> load() {
            return upstream -> upstream.flatMap((Function<Boolean, ObservableSource<DownloadBitmap>>) force -> {
                Wallpaper image;
                if (!force) {
                    Intent intent = BingWallpaperUtils.checkRunningServiceIntent(mContext,
                            TAG, false);
                    if (intent == null) {
                        return null;
                    }
                }
                try {
                    image = BingWallpaperUtils.getImage(getApplicationContext(), true);
                    return Observable.just(new DownloadBitmap(image));
                } catch (IOException e) {
                    return Observable.error(e);
                }
            });
        }

        private ObservableTransformer<DownloadBitmap, DownloadBitmap> download() {
            return upstream -> upstream.flatMap((Function<DownloadBitmap, ObservableSource<DownloadBitmap>>) image -> {
                File wallpaper = BingWallpaperUtils.getGlideFile(mContext, image.image.getImageUrl());
                int stackBlur = BingWallpaperUtils.getSettingStackBlur(mContext);
                if (stackBlur > 0) {
                    String key = MD5Utils.md5Hex(wallpaper.getAbsolutePath() + "_" + stackBlur);
                    File stackBlurFile = CacheUtils.get().get(key);
                    if (stackBlurFile == null) {
                        image.bitmap = BingWallpaperUtils.toStackBlur(
                                BitmapFactory.decodeFile(wallpaper.getAbsolutePath()), stackBlur);
                        CacheUtils.get().put(key, BitmapUtils.bitmapToStream(image.bitmap,
                                Bitmap.CompressFormat.JPEG));
                    } else {
                        image.bitmap = BitmapFactory.decodeFile(stackBlurFile.getAbsolutePath());
                    }
                } else {
                    image.bitmap = BitmapFactory.decodeFile(wallpaper.getAbsolutePath());
                }
                if (BingWallpaperUtils.isTaskUndone(mContext)) {
                    BingWallpaperUtils.autoSaveWallpaper(mContext, TAG, image.image, wallpaper);
                }
                return Observable.just(image);
            });
        }

        private void draw(Bitmap bitmap) {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;
            try {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    canvas.drawColor(Color.BLACK);
                    //canvas.drawBitmap(bitmap, 0, 0, paint);
                    scaleCenterCrop(bitmap, canvas, height, width);
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }

        //https://stackoverflow.com/questions/8112715/how-to-crop-bitmap-center-like-imageview
        public void scaleCenterCrop(Bitmap source, Canvas canvas, int newHeight, int newWidth) {
            int sourceWidth = source.getWidth();
            int sourceHeight = source.getHeight();

            float xScale = (float) newWidth / sourceWidth;
            float yScale = (float) newHeight / sourceHeight;
            float scale = Math.max(xScale, yScale);

            float scaledWidth = scale * sourceWidth;
            float scaledHeight = scale * sourceHeight;

            float left = (newWidth - scaledWidth) / 2;
            float top = (newHeight - scaledHeight) / 2;

            RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

            canvas.drawBitmap(source, null, targetRect, null);
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            if (mEngine != null) {
                mEngine.init();
            }
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            Utils.dispose(mDisposable);
            handler.removeCallbacks(drawRunner);
            super.onSurfaceDestroyed(holder);
        }
    }

}
