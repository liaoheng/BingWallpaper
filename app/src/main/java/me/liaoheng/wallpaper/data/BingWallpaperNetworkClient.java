package me.liaoheng.wallpaper.data;

import android.content.Context;
import com.github.liaoheng.common.util.*;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import me.liaoheng.wallpaper.model.*;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.Constants;
import me.liaoheng.wallpaper.util.NetUtils;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;

/**
 * @author liaoheng
 * @version 2016-09-20 11:27
 */
public class BingWallpaperNetworkClient {

    public static Observable<BingWallpaperImage> getBingWallpaper(Context context) {
        return getBingWallpaper(context, 0, 1).map(bingWallpaperImages -> bingWallpaperImages.get(0));
    }

    public static Observable<List<BingWallpaperImage>> getBingWallpaper(Context context, int index,
            int count) {
        String url = BingWallpaperUtils.getUrl(context, index, count);
        String locale = BingWallpaperUtils.getAutoLocale(context);
        return getBingWallpaper(url, locale).map(bingWallpaper -> {
            if (bingWallpaper == null || bingWallpaper.getImages() == null || bingWallpaper.getImages().isEmpty()) {
                throw new SystemRuntimeException(new SystemDataException("bing wallpaper is not data"));
            }
            return bingWallpaper.getImages();
        });
    }

    public static Observable<BingWallpaper> getBingWallpaper(String url, String locale) {
        return NetUtils.get().getBingWallpaperNetworkService()
                .getBingWallpaper(url, getMkt(locale)).subscribeOn(Schedulers.io());
    }

    public static Observable<BingWallpaperImage> getBingWallpaperSingle(Context context) {
        String url = BingWallpaperUtils.getUrl(context, 0, 1);
        String locale = BingWallpaperUtils.getAutoLocale(context);
        return getBingWallpaperSingle(url, locale);
    }

    public static Observable<BingWallpaperImage> getBingWallpaperSingle(String url, String locale) {
        return NetUtils.get().getBingWallpaperSingleNetworkService()
                .getBingWallpaper(url, getMkt(locale)).subscribeOn(Schedulers.io())
                .map(bingWallpaper -> {
                    if (bingWallpaper == null || bingWallpaper.getImages() == null
                            || bingWallpaper.getImages().isEmpty()) {
                        throw new SystemRuntimeException(new SystemDataException("bing wallpaper is not data"));
                    }
                    return bingWallpaper.getImages().get(0);
                });
    }

    public static BingWallpaperImage getBingWallpaperSingleCall(String url, String locale) throws NetException {
        try {
            Response<BingWallpaper> execute = NetUtils.get().getBingWallpaperSingleNetworkService()
                    .getBingWallpaperCall(url, getMkt(locale)).execute();
            if (execute.isSuccessful()) {
                BingWallpaper bingWallpaper = execute.body();
                if (bingWallpaper == null || bingWallpaper.getImages() == null
                        || bingWallpaper.getImages().isEmpty()) {
                    throw new NetServerException("bing wallpaper is not data");
                }
                return bingWallpaper.getImages().get(0);
            } else {
                throw new NetServerException("bing server response failure");
            }
        } catch (IOException e) {
            throw new NetLocalException(e);
        }
    }

    public static Observable<BingWallpaperCoverStory> getCoverStory() {
        return NetUtils.get().getBingWallpaperNetworkService().getCoverStory().subscribeOn(Schedulers.io());
    }

    private static String getMkt(String locale) {
        return String.format(Constants.MKT_HEADER, locale);
    }

    public static PixabayImage getPixabayEditorsChoiceExecute() throws NetException {
        try {
            Response<Pixabay> execute = NetUtils.get()
                    .getBingWallpaperSingleNetworkService()
                    .getPixabayEditorsChoice()
                    .execute();
            if (execute.isSuccessful()) {
                Pixabay bingWallpaper = execute.body();
                if (bingWallpaper == null || bingWallpaper.getHits() == null
                        || bingWallpaper.getHits().isEmpty()) {
                    throw new NetServerException("pixabay is not data");
                }
                int size = bingWallpaper.getHits().size();
                int num = (int) (Math.random() * size);
                return bingWallpaper.getHits().get(num);
            } else {
                throw new NetServerException("pixabay server response failure");
            }
        } catch (IOException e) {
            throw new NetLocalException(e);
        }
    }
}
