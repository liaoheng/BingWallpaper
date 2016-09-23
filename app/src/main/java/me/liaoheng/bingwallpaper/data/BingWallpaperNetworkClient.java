package me.liaoheng.bingwallpaper.data;

import android.content.Context;
import android.util.AndroidRuntimeException;
import com.github.liaoheng.common.util.L;
import me.liaoheng.bingwallpaper.MApplication;
import me.liaoheng.bingwallpaper.model.BingWallpaper;
import me.liaoheng.bingwallpaper.model.BingWallpaperImage;
import me.liaoheng.bingwallpaper.util.SettingsUtils;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * @author liaoheng
 * @version 2016-09-20 11:27
 */
public class BingWallpaperNetworkClient {

    private static final String TAG = BingWallpaperNetworkClient.class.getSimpleName();

    public static Observable<BingWallpaperImage> getBingWallpaper(Context context) {
        String url = SettingsUtils.getUrl(context);
        L.Log.i(TAG, "getBingWallpaper url :%s", url);
        return MApplication.getInstance().getRetrofit().create(BingWallpaperNetworkService.class)
                .getBingWallpaper(url).subscribeOn(Schedulers.io())
                .map(new Func1<BingWallpaper, BingWallpaperImage>() {
                    @Override public BingWallpaperImage call(BingWallpaper bingWallpaper) {
                        if (bingWallpaper == null || bingWallpaper.getImages() == null
                            || bingWallpaper.getImages().isEmpty()) {
                            throw new AndroidRuntimeException("没有bing壁纸数据！");
                        }
                        return bingWallpaper.getImages().get(0);
                    }
                });
    }

}
