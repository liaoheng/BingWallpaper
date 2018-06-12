package me.liaoheng.wallpaper.data;

import me.liaoheng.wallpaper.model.BingWallpaper;
import me.liaoheng.wallpaper.model.BingWallpaperCoverStory;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;
import rx.Observable;

/**
 * @author liaoheng
 * @version 2016-09-19 11:15
 */
public interface BingWallpaperNetworkService {
    @GET Observable<BingWallpaper> getBingWallpaper(@Url String url);
    @GET Call<BingWallpaper> getBingWallpaperCall(@Url String url);
    @GET("https://cn.bing.com/cnhp/coverstory/") Observable<BingWallpaperCoverStory> getCoverstory();
}
