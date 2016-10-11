package me.liaoheng.bingwallpaper.service;

import android.app.IntentService;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AndroidRuntimeException;
import com.bumptech.glide.Glide;
import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.NetworkUtils;
import java.io.File;
import me.liaoheng.bingwallpaper.data.BingWallpaperNetworkClient;
import me.liaoheng.bingwallpaper.model.BingWallpaperImage;
import me.liaoheng.bingwallpaper.model.BingWallpaperState;
import me.liaoheng.bingwallpaper.util.LogDebugFileUtils;
import me.liaoheng.bingwallpaper.util.Utils;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * @author liaoheng
 * @version 2016-9-19 12:48
 */
public class BingWallpaperIntentService extends IntentService {

    private final       String TAG                        = BingWallpaperIntentService.class
            .getSimpleName();
    public final static String ACTION_GET_WALLPAPER_STATE = "me.liaoheng.bingwallpaper.BING_WALLPAPER_STATE";
    public final static String EXTRA_WALLPAPER_STATE      = "STATE";

    public BingWallpaperIntentService() {
        super("BingWallpaperIntentService");
    }

    @Override protected void onHandleIntent(final Intent intent) {
        if (Utils.isEnableLog(getApplicationContext())) {
            LogDebugFileUtils.get().i(TAG, "Run BingWallpaperIntentService");
        }

        if (NetworkUtils.isConnected(getApplicationContext())) {

            if (Utils.getOnlyWifi(getApplicationContext())) {
                if (!NetworkUtils.isWifiAvailable(getApplicationContext())) {
                    return;
                }
            }

            Intent intent1 = new Intent(BingWallpaperIntentService.ACTION_GET_WALLPAPER_STATE);
            intent1.putExtra(EXTRA_WALLPAPER_STATE, BingWallpaperState.BEGIN);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent1);
            L.Log.i(TAG, "getBingWallpaper start");
            if (Utils.isEnableLog(getApplicationContext())) {
                LogDebugFileUtils.get().i(TAG, "is ok , Start getBingWallpaper");
            }

            BingWallpaperNetworkClient.getBingWallpaper(getApplicationContext())
                    .flatMap(new Func1<BingWallpaperImage, Observable<File>>() {
                        @Override public Observable<File> call(
                                BingWallpaperImage bingWallpaperImage) {
                            String url = Utils.getUrl(getApplicationContext(), bingWallpaperImage);
                            L.Log.i(TAG, "wallpaper url : %s", url);
                            try {
                                File wallpaper = Glide.with(getApplicationContext()).load(url)
                                        .downloadOnly(0, 0).get();
                                String absolutePath = wallpaper.getAbsolutePath();
                                L.Log.i(TAG, "wallpaper file : " + absolutePath);

                                Bitmap bitmap = BitmapFactory.decodeFile(absolutePath);
                                WallpaperManager.getInstance(getApplicationContext())
                                        .setBitmap(bitmap);

                                return Observable.just(wallpaper);
                            } catch (Exception e) {
                                throw new AndroidRuntimeException(e);
                            }
                        }
                    }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<File>() {
                @Override public void call(File file) {
                    L.Log.i(TAG, "getBingWallpaper Success");
                    Intent intent1 = new Intent(
                            BingWallpaperIntentService.ACTION_GET_WALLPAPER_STATE);
                    intent1.putExtra(EXTRA_WALLPAPER_STATE, BingWallpaperState.SUCCESS);
                    LocalBroadcastManager.getInstance(getApplicationContext())
                            .sendBroadcast(intent1);
                }
            }, new Action1<Throwable>() {
                @Override public void call(Throwable throwable) {
                    L.Log.e(TAG, throwable, "getBingWallpaper Error");
                    if (Utils.isEnableLog(getApplicationContext())) {
                        LogDebugFileUtils.get().e(TAG, throwable, "getBingWallpaper Error");
                    }
                    Intent intent1 = new Intent(
                            BingWallpaperIntentService.ACTION_GET_WALLPAPER_STATE);
                    intent1.putExtra(EXTRA_WALLPAPER_STATE, BingWallpaperState.FAIL);
                    LocalBroadcastManager.getInstance(getApplicationContext())
                            .sendBroadcast(intent1);
                }
            });
        }

    }
}
