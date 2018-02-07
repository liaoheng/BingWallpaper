package me.liaoheng.wallpaper.data;

import com.github.liaoheng.common.util.SystemDataException;
import com.github.liaoheng.common.util.SystemRuntimeException;

import java.util.List;

import me.liaoheng.wallpaper.model.BingWallpaper;
import me.liaoheng.wallpaper.model.BingWallpaperImage;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.NetUtils;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * @author liaoheng
 * @version 2016-09-20 11:27
 */
public class BingWallpaperNetworkClient {

    public static Observable<BingWallpaperImage> getBingWallpaper() {
        String url = BingWallpaperUtils.getUrl();
        return getBingWallpaper(url).map(new Func1<BingWallpaper, BingWallpaperImage>() {
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

    public static Observable<List<BingWallpaperImage>> getBingWallpaper(int index, int count) {
        String url = BingWallpaperUtils.getUrl(index, count);
        return getBingWallpaper(url).map(new Func1<BingWallpaper, List<BingWallpaperImage>>() {
            @Override
            public List<BingWallpaperImage> call(BingWallpaper bingWallpaper) {
                if (bingWallpaper == null || bingWallpaper.getImages() == null) {
                    throw new SystemRuntimeException(new SystemDataException("bing wallpaper is not data"));
                }
                return bingWallpaper.getImages();
            }
        });
    }

    public static Observable<BingWallpaper> getBingWallpaper(String url) {
        return NetUtils.get().getBingWallpaperNetworkService()
                .getBingWallpaper(url).subscribeOn(Schedulers.io());
    }

    public static Observable<BingWallpaperImage> getBingWallpaperSingle() {
        String url = BingWallpaperUtils.getUrl();
        return NetUtils.get().getBingWallpaperSingleNetworkService()
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
