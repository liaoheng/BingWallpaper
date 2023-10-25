package me.liaoheng.wallpaper.data;

import io.reactivex.rxjava3.core.Observable;
import me.liaoheng.wallpaper.model.BingWallpaper;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Url;

import static me.liaoheng.wallpaper.util.Constants.USER_AGENT;

/**
 * @author liaoheng
 * @version 2016-09-19 11:15
 */
public interface BingWallpaperNetworkService {

    @Headers({
            "User-Agent:" + USER_AGENT,
            "Cache-Control:public, max-age=" + 60 * 60 * 24
    })
    @GET
    Observable<BingWallpaper> getBingWallpaper(@Url String url, @Header("Cookie") String mkt);

    @Headers({
            "User-Agent:" + USER_AGENT,
    })
    @GET
    Call<BingWallpaper> getBingWallpaperCall(@Url String url, @Header("Cookie") String mkt,
            @Header("Cache-Control") String cache);
}
