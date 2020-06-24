package me.liaoheng.wallpaper.util;

import android.content.Context;
import android.net.Uri;

import androidx.preference.PreferenceManager;

import com.bumptech.glide.request.target.Target;
import com.github.liaoheng.common.util.Callback;
import com.github.liaoheng.common.util.FileUtils;
import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.SystemRuntimeException;
import com.github.liaoheng.common.util.Utils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import me.liaoheng.wallpaper.BuildConfig;
import me.liaoheng.wallpaper.data.BingWallpaperNetworkService;
import okhttp3.Cache;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.dnsoverhttps.DnsOverHttps;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author liaoheng
 * @version 2016-11-15 10:48
 */
public class NetUtils {

    private NetUtils() {
    }

    private static NetUtils retrofitUtils;

    public static NetUtils get() {
        if (retrofitUtils == null) {
            retrofitUtils = new NetUtils();
        }
        return retrofitUtils;
    }

    private Retrofit mRetrofit;

    private OkHttpClient client;

    public void clearCache() {
        if (client.cache() == null) {
            return;
        }
        try {
            client.cache().evictAll();
        } catch (IOException ignored) {
        }
    }

    public OkHttpClient.Builder initOkHttpClientBuilder(Context context, long readTimeout, long connectTimeout) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.readTimeout(readTimeout, TimeUnit.SECONDS)
                .connectTimeout(connectTimeout, TimeUnit.SECONDS);
        if (PreferenceManager
                .getDefaultSharedPreferences(context).getBoolean("pref_doh", false)) {
            builder.dns(new DnsOverHttps.Builder()
                    .client(new OkHttpClient.Builder().build())
                    .url(HttpUrl.get("https://cloudflare-dns.com/dns-query"))
                    .build());
        }
        return builder;
    }

    public void init(Context context) {
        //Security.insertProviderAt(Conscrypt.newProvider(), 1);
        Retrofit.Builder factory = new Retrofit.Builder().baseUrl(Constants.LOCAL_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create());
        OkHttpClient.Builder simpleBuilder = initOkHttpClientBuilder(context, 60, 30);
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> L.alog().d("NetUtils", message));
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        if (BuildConfig.DEBUG) {
            simpleBuilder.addInterceptor(logging);
        }
        try {
            File cacheFile = FileUtils.getProjectSpaceCacheDirectory(context, Constants.HTTP_CACHE_DIR);
            simpleBuilder.cache(new Cache(cacheFile, Constants.HTTP_DISK_CACHE_SIZE));
        } catch (IOException ignored) {
        }
        client = simpleBuilder.build();
        mRetrofit = factory.client(client).build();
    }

    private BingWallpaperNetworkService mBingWallpaperNetworkService;

    public BingWallpaperNetworkService getBingWallpaperNetworkService() {
        if (mBingWallpaperNetworkService == null) {
            mBingWallpaperNetworkService = mRetrofit.create(BingWallpaperNetworkService.class);
        }
        return mBingWallpaperNetworkService;
    }

    public Disposable downloadImageToFile(final Context context, String url, Callback<Uri> callback) {
        Observable<Uri> observable = Observable.just(url).subscribeOn(Schedulers.io())
                .map(url1 -> {
                    File temp = null;
                    try {
                        temp = GlideApp.with(context)
                                .asFile()
                                .load(url1)
                                .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                                .get(2, TimeUnit.MINUTES);
                        return BingWallpaperUtils.saveFileToPictureCompat(context, url1, temp);
                    } catch (Exception e) {
                        throw new SystemRuntimeException(e);
                    } finally {
                        if (temp != null) {
                            FileUtils.delete(temp);
                        }
                    }
                });
        return Utils.addSubscribe(observable, callback);
    }
}
