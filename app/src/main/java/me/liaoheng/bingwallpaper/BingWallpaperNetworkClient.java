package me.liaoheng.bingwallpaper;

import android.content.Context;
import android.util.AndroidRuntimeException;
import me.liaoheng.bingwallpaper.model.BingWallpaper;
import me.liaoheng.bingwallpaper.model.BingWallpaperImage;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * @author liaoheng
 * @version 2016-09-20 11:27
 */
public class BingWallpaperNetworkClient {

    public static Observable<BingWallpaperImage> getBingWallpaper(Context context) {
        String url = SettingsUtils.getUrl(context);
        return App.getInstance().getRetrofit().create(BingWallpaperNetworkService.class)
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
