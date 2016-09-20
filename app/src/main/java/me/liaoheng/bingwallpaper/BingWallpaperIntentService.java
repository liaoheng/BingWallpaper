package me.liaoheng.bingwallpaper;

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
import me.liaoheng.bingwallpaper.model.BingWallpaperImage;
import me.liaoheng.bingwallpaper.model.BingWallpaperState;
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
    public final static String ACTION_GET_WALLPAPER_STATE = "BING_WALLPAPER_STATE";
    public final static String EXTRA_WALLPAPER_STATE      = "STATE";

    public BingWallpaperIntentService() {
        super("BingWallpaperIntentService");
    }

    @Override protected void onHandleIntent(final Intent intent) {
        if (NetworkUtils.isConnected(getApplicationContext())) {

            if (SettingsUtils.getOnlyWifi(getApplicationContext())) {
                if (!NetworkUtils.isWifiAvailable(getApplicationContext())) {
                    return;
                }
            }

            Intent intent1 = new Intent(BingWallpaperIntentService.ACTION_GET_WALLPAPER_STATE);
            intent1.putExtra(EXTRA_WALLPAPER_STATE, BingWallpaperState.BEGIN);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent1);
            L.Log.i(TAG, "getBingWallpaper start");

            BingWallpaperNetworkClient.getBingWallpaper(getApplicationContext())
                    .flatMap(new Func1<BingWallpaperImage, Observable<File>>() {
                        @Override public Observable<File> call(
                                BingWallpaperImage bingWallpaperImage) {
                            String urlbase = bingWallpaperImage.getUrlbase();

                            String resolution = SettingsUtils
                                    .getResolution(getApplicationContext());

                            String url = Constants.BASE_URL + urlbase + "_" + resolution + ".jpg";
                            L.Log.i(TAG, "wallpaper url : " + url);
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
                    L.Log.i(TAG, "getBingWallpaper success");
                    Intent intent1 = new Intent(
                            BingWallpaperIntentService.ACTION_GET_WALLPAPER_STATE);
                    intent1.putExtra(EXTRA_WALLPAPER_STATE, BingWallpaperState.SUCCESS);
                    LocalBroadcastManager.getInstance(getApplicationContext())
                            .sendBroadcast(intent1);
                }
            }, new Action1<Throwable>() {
                @Override public void call(Throwable throwable) {
                    L.Log.e(TAG, "getBingWallpaper error", throwable);
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
