package me.liaoheng.bingwallpaper;

import android.app.IntentService;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v4.content.LocalBroadcastManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.NetworkUtils;
import com.github.liaoheng.common.util.ScreenUtils;
import java.io.File;
import java.io.IOException;
import me.liaoheng.bingwallpaper.model.BingWallpaper;
import me.liaoheng.bingwallpaper.model.BingWallpaperImage;
import me.liaoheng.bingwallpaper.model.BingWallpaperState;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * @author liaoheng
 * @version 2016-9-19 12:48
 */
public class BingWallpaperIntentService extends IntentService {

    private final       String TAG         = BingWallpaperIntentService.class.getSimpleName();
    public final static String AUTO        = "auto";
    public final static String ACTION_GET_WALLPAPER_STATE      = "BING_WALLPAPER_STATE";
    public final static String EXTRA_WALLPAPER_IMAGE = "IMAGE";
    public final static String EXTRA_WALLPAPER_STATE = "STATE";

    public BingWallpaperIntentService() {
        super("BingWallpaperIntentService");
    }

    @Override protected void onHandleIntent(final Intent intent) {
        final boolean auto = intent.getBooleanExtra(AUTO, true);
        if (NetworkUtils.isConnected(getApplicationContext()) && NetworkUtils
                .isWifiAvailable(getApplicationContext())) {
            if (!auto) {
                Intent intent1 = new Intent(BingWallpaperIntentService.ACTION_GET_WALLPAPER_STATE);
                intent1.putExtra(EXTRA_WALLPAPER_STATE, BingWallpaperState.BEGIN);
                LocalBroadcastManager.getInstance(getApplicationContext())
                        .sendBroadcast(intent1);
            }

            Observable<BingWallpaper> bingWallpaper = App.getInstance().getRetrofit()
                    .create(BingWallpaperNetworkService.class).getBingWallpaper(
                            "http://www.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1");
            bingWallpaper.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<BingWallpaper>() {
                        @Override public void call(BingWallpaper bingWallpaper) {
                            if (bingWallpaper == null || bingWallpaper.getImages() == null
                                || bingWallpaper.getImages().isEmpty()) {
                                return;
                            }
                            BingWallpaperImage bingWallpaperImage = bingWallpaper.getImages()
                                    .get(0);

                            if (!auto) {
                                L.Log.i(TAG, "is no auto: send LocalBroadcast");
                                Intent intent1 = new Intent(BingWallpaperIntentService.ACTION_GET_WALLPAPER_STATE);
                                intent1.putExtra(EXTRA_WALLPAPER_STATE,BingWallpaperState.SUCCESS);
                                intent1.putExtra(EXTRA_WALLPAPER_IMAGE, bingWallpaperImage);
                                LocalBroadcastManager.getInstance(getApplicationContext())
                                        .sendBroadcast(intent1);
                                return;
                            }

                            int screenWidth = ScreenUtils.getScreenWidth(getApplicationContext());
                            int screenHeight = ScreenUtils.getScreenHeight(getApplicationContext());
                            String url = bingWallpaperImage.getUrl();
                            Glide.with(getApplicationContext()).load(url).downloadOnly(
                                    new SimpleTarget<File>(screenWidth, screenHeight) {
                                        @Override public void onResourceReady(File wallpaper,
                                                                              GlideAnimation<? super File> glideAnimation) {
                                            String absolutePath = wallpaper.getAbsolutePath();
                                            L.Log.i(TAG, "file: " + absolutePath);

                                            try {
                                                Bitmap bitmap = BitmapFactory
                                                        .decodeFile(absolutePath);
                                                WallpaperManager
                                                        .getInstance(getApplicationContext())
                                                        .setBitmap(bitmap);
                                            } catch (IOException e) {
                                                onLoadFailed(e, null);
                                            }
                                        }

                                        @Override public void onLoadFailed(Exception e,
                                                                           Drawable errorDrawable) {
                                            if (!auto) {
                                                Intent intent1 = new Intent(BingWallpaperIntentService.ACTION_GET_WALLPAPER_STATE);
                                                intent1.putExtra(EXTRA_WALLPAPER_STATE, BingWallpaperState.FAIL);
                                                LocalBroadcastManager.getInstance(getApplicationContext())
                                                        .sendBroadcast(intent1);
                                            }
                                            //else{
                                            //    L.getToast().e(TAG, getApplicationContext(), e);
                                            //}
                                        }
                                    });
                        }
                    }, new Action1<Throwable>() {
                        @Override public void call(Throwable throwable) {
                            L.getToast().e(TAG, getApplicationContext(), throwable);
                        }
                    });
        }

    }
}
