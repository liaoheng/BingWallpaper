package me.liaoheng.wallpaper.data;

import io.reactivex.Observable;
import me.liaoheng.wallpaper.model.BingWallpaper;
import me.liaoheng.wallpaper.model.BingWallpaperCoverStory;
import me.liaoheng.wallpaper.model.Pixabay;
import retrofit2.Call;
import retrofit2.http.*;

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

    @GET("https://pixabay.com/api/?key=11234205-21f02ee751cd3cd4f1fa49b70&editors_choice=true&image_type=photo")
    Call<Pixabay> getPixabayEditorsChoice();

    @GET("https://pixabay.com/api/?key=11234205-21f02ee751cd3cd4f1fa49b70&editors_choice=true&image_type=photo")
    Observable<Pixabay> getPixabayEditorsChoice(@Query("page") int page, @Query("per_page") int perPage);
}
