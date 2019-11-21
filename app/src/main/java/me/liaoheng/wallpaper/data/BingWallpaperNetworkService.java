package me.liaoheng.wallpaper.data;

import io.reactivex.Observable;
import me.liaoheng.wallpaper.model.BingWallpaper;
import me.liaoheng.wallpaper.model.BingWallpaperCoverStory;
import me.liaoheng.wallpaper.model.Pixabay;
import retrofit2.Call;
import retrofit2.http.*;

import static me.liaoheng.wallpaper.util.Constants.PIXABAY_BASE_URL;

/**
 * @author liaoheng
 * @version 2016-09-19 11:15
 */
public interface BingWallpaperNetworkService {

    @GET
    Observable<BingWallpaper> getBingWallpaper(@Url String url, @Header("Cookie") String mkt);

    @GET
    Call<BingWallpaper> getBingWallpaperCall(@Url String url, @Header("Cookie") String mkt);

    @Headers({
            "User-Agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:61.0) Gecko/20100101 Firefox/61.0",
            "Cookie: _EDGE_S=mkt=zh-cn"
    })
    @GET("https://www.bing.com/cnhp/coverstory")
    Observable<BingWallpaperCoverStory> getCoverStory();

    @GET(PIXABAY_BASE_URL)
    Call<Pixabay> getPixabays(@Query("per_page") int perPage);

    @GET(PIXABAY_BASE_URL)
    Observable<Pixabay> getPixabays(@Query("page") int page, @Query("per_page") int perPage,@Query("order") String order);
}
