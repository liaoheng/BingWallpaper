package me.liaoheng.wallpaper.data;

import android.content.Context;
import android.util.AndroidRuntimeException;

import com.github.liaoheng.common.util.ValidateUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import me.liaoheng.wallpaper.model.BingWallpaper;
import me.liaoheng.wallpaper.model.BingWallpaperCoverStory;
import me.liaoheng.wallpaper.model.BingWallpaperImage;
import me.liaoheng.wallpaper.model.Pixabay;
import me.liaoheng.wallpaper.model.PixabayImage;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.Constants;
import me.liaoheng.wallpaper.util.NetUtils;
import retrofit2.Response;

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
        String locale = BingWallpaperUtils.getAutoLocale(context);
        String url = BingWallpaperUtils.getUrl(context, index, count, locale);
        return getBingWallpaper(url, locale).map(bingWallpaper -> {
            if (bingWallpaper == null || bingWallpaper.getImages() == null || bingWallpaper.getImages().isEmpty()) {
                throw new AndroidRuntimeException(new IOException("bing wallpaper is not data"));
            }
            return bingWallpaper.getImages();
        });
    }

    public static Observable<BingWallpaper> getBingWallpaper(String url, String locale) {
        return NetUtils.get().getBingWallpaperNetworkService()
                .getBingWallpaper(url, getMkt(locale)).subscribeOn(Schedulers.io());
    }

    public static Observable<BingWallpaperImage> getBingWallpaperSingle(Context context) {
        String locale = BingWallpaperUtils.getAutoLocale(context);
        String url = BingWallpaperUtils.getUrl(context, 0, 1, locale);
        return getBingWallpaperSingle(url, locale);
    }

    public static Observable<BingWallpaperImage> getBingWallpaperSingle(String url, String locale) {
        return NetUtils.get().getBingWallpaperSingleNetworkService()
                .getBingWallpaper(url, getMkt(locale)).subscribeOn(Schedulers.io())
                .map(bingWallpaper -> {
                    if (bingWallpaper == null || bingWallpaper.getImages() == null
                            || bingWallpaper.getImages().isEmpty()) {
                        throw new AndroidRuntimeException(new IOException("bing wallpaper is not data"));
                    }
                    return bingWallpaper.getImages().get(0);
                });
    }

    public static BingWallpaperImage getBingWallpaperSingleCall(Context context) throws IOException {
        String locale = BingWallpaperUtils.getAutoLocale(context);
        String url = BingWallpaperUtils.getUrl(context);
        return getBingWallpaperSingleCall(url, locale);
    }

    public static BingWallpaperImage getBingWallpaperSingleCall(String url, String locale) throws IOException {
        Response<BingWallpaper> execute = NetUtils.get().getBingWallpaperSingleNetworkService()
                .getBingWallpaperCall(url, getMkt(locale)).execute();
        if (execute.isSuccessful()) {
            BingWallpaper bingWallpaper = execute.body();
            if (bingWallpaper == null || ValidateUtils.isItemEmpty(bingWallpaper.getImages())) {
                throw new IOException("bing wallpaper is not data");
            }
            return bingWallpaper.getImages().get(0);
        } else {
            throw new IOException("bing server response failure");
        }
    }

    @Deprecated
    public static Observable<BingWallpaperCoverStory> getCoverStory() {
        return NetUtils.get().getBingWallpaperNetworkService().getCoverStory().subscribeOn(Schedulers.io());
    }

    private static String getMkt(String locale) {
        return String.format(Constants.MKT_HEADER, locale);
    }

    private static int PIXABAY_EDITORS_CHOICE_PER_PAGE = 60;

    public static BingWallpaperImage getPixabaysExecute() throws IOException {
        Response<Pixabay> execute = NetUtils.get()
                .getBingWallpaperSingleNetworkService()
                .getPixabays(PIXABAY_EDITORS_CHOICE_PER_PAGE)
                .execute();
        if (execute.isSuccessful()) {
            Pixabay pixabay = execute.body();
            if (pixabay == null || ValidateUtils.isItemEmpty(pixabay.getHits())) {
                throw new IOException("pixabay is not data");
            }
            return createBingWallpaperImage(randomPixabay(pixabay));
        } else {
            throw new IOException("pixabay server response failure");
        }
    }

    public static Observable<Pixabay> getPixabays(int page, int perPage, String order) {
        return NetUtils.get()
                .getBingWallpaperNetworkService()
                .getPixabays(page, perPage, order)
                .subscribeOn(Schedulers.io());
    }

    private static BingWallpaperImage createBingWallpaperImage(PixabayImage image) {
        BingWallpaperImage bingWallpaper = new BingWallpaperImage(
                "Photo by " + image.getUser() + " on Pixabay");
        bingWallpaper.setUrl(image.getLargeImageURL());
        bingWallpaper.setUrlbase(image.getPreviewURL());
        bingWallpaper.setCopyrightlink(image.getPageURL());
        return bingWallpaper;
    }

    public static Observable<List<BingWallpaperImage>> getPixabays(int page) {
        return getPixabays(page, 20,"latest").flatMap(
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
        try {
            int size = pixabay.getHits().size();
            int num = new Random().nextInt(size);
            return pixabay.getHits().get(num);
        } catch (Exception e) {
            return pixabay.getHits().get(0);
        }
    }

    public static Observable<BingWallpaperImage> randomPixabayImage() {
        return getPixabays(1, PIXABAY_EDITORS_CHOICE_PER_PAGE,"popular").flatMap(
                (Function<Pixabay, ObservableSource<BingWallpaperImage>>) pixabay -> {
                    if (ValidateUtils.isItemEmpty(pixabay.getHits())) {
                        return Observable.error(new IOException("pixabay is not data"));
                    }
                    return Observable.just(createBingWallpaperImage(randomPixabay(pixabay)));
                });
    }
}
