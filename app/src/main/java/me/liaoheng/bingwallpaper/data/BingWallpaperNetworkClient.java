package me.liaoheng.bingwallpaper.data;

import android.content.Context;

import com.github.liaoheng.common.util.SystemDataException;
import com.github.liaoheng.common.util.SystemRuntimeException;

import me.liaoheng.bingwallpaper.model.BingWallpaper;
import me.liaoheng.bingwallpaper.model.BingWallpaperImage;
import me.liaoheng.bingwallpaper.util.BUtils;
import me.liaoheng.bingwallpaper.util.LogDebugFileUtils;
import me.liaoheng.bingwallpaper.util.NetUtils;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * @author liaoheng
 * @version 2016-09-20 11:27
 */
public class BingWallpaperNetworkClient {

    public static Observable<BingWallpaperImage> getBingWallpaper(Context context) {
        String url = BUtils.getUrl();
        if (BUtils.isEnableLog(context)) {
            LogDebugFileUtils.get()
                    .i("BingWallpaperNetworkClient", "getBingWallpaper url :%s", url);
        }
        return NetUtils.get().getBingWallpaperNetworkService()
                .getBingWallpaper(url).subscribeOn(Schedulers.io())
                .map(new Func1<BingWallpaper, BingWallpaperImage>() {
                    @Override
                    public BingWallpaperImage call(BingWallpaper bingWallpaper) {
                        if (bingWallpaper == null || bingWallpaper.getImages() == null
                                || bingWallpaper.getImages().isEmpty()) {
                            throw new SystemRuntimeException(new SystemDataException("bing wallpaper is not data"));
                        }
                        return bingWallpaper.getImages().get(0);
                    }
                });
    }

}
