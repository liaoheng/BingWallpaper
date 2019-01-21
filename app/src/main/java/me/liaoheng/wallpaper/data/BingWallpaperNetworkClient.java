package me.liaoheng.wallpaper.data;

import android.content.Context;
import com.github.liaoheng.common.util.*;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import me.liaoheng.wallpaper.model.*;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.Constants;
import me.liaoheng.wallpaper.util.NetUtils;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
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

    public static BingWallpaperImage getPixabayEditorsChoiceExecute() throws NetException {
        try {
            Response<Pixabay> execute = NetUtils.get()
                    .getBingWallpaperSingleNetworkService()
                    .getPixabayEditorsChoice()
                    .execute();
            if (execute.isSuccessful()) {
                Pixabay pixabay = execute.body();
                if (pixabay == null || pixabay.getHits() == null
                        || pixabay.getHits().isEmpty()) {
                    throw new NetServerException("pixabay is not data");
                }
                return createBingWallpaperImage(randomPixabay(pixabay));
            } else {
                throw new NetServerException("pixabay server response failure");
            }
        } catch (IOException e) {
            throw new NetLocalException(e);
        }
    }

    public static Observable<Pixabay> getPixabayEditorsChoice(int page, int perPage) {
        return NetUtils.get()
                .getBingWallpaperNetworkService()
                .getPixabayEditorsChoice(page, perPage)
                .subscribeOn(Schedulers.io());
    }

    private static BingWallpaperImage createBingWallpaperImage(PixabayImage image) {
        BingWallpaperImage bingWallpaper = new BingWallpaperImage(
                "Photo by " + image.getUser() + " on Pixabay");
        bingWallpaper.setUrl(image.getLargeImageURL().replace("_1280", "_960"));
        bingWallpaper.setUrlbase(image.getPreviewURL());
        bingWallpaper.setCopyrightlink(image.getPageURL());
        return bingWallpaper;
    }

    public static Observable<List<BingWallpaperImage>> getPixabayEditorsChoiceList(int page) {
        return getPixabayEditorsChoice(page, 20).flatMap(
                (Function<Pixabay, ObservableSource<List<BingWallpaperImage>>>) pixabay -> {
                    List<BingWallpaperImage> wallpaperList = new ArrayList<>();
                    if (ValidateUtils.isItemEmpty(pixabay.getHits())) {
                        return Observable.just(wallpaperList);
                    }
                    for (PixabayImage image : pixabay.getHits()) {
                        wallpaperList.add(createBingWallpaperImage(image));
                    }
                    return Observable.just(wallpaperList);
                });
    }

    public static PixabayImage randomPixabay(Pixabay pixabay) {
        int size = pixabay.getHits().size();
        int num = (int) (Math.random() * size);
        return pixabay.getHits().get(num);
    }

    public static Observable<BingWallpaperImage> randomPixabayEditorsChoice() {
        return getPixabayEditorsChoice(1, 60).flatMap(
                (Function<Pixabay, ObservableSource<BingWallpaperImage>>) pixabay -> {
                    if (ValidateUtils.isItemEmpty(pixabay.getHits())) {
                        return Observable.error(new NetServerException("pixabay is not data"));
                    }
                    return Observable.just(createBingWallpaperImage(randomPixabay(pixabay)));
                });
    }
}
