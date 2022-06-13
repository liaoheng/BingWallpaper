package me.liaoheng.wallpaper.util;

import android.content.Context;
import android.net.Uri;

import androidx.preference.PreferenceManager;

import com.bumptech.glide.request.target.Target;
import com.github.liaoheng.common.util.Callback;
import com.github.liaoheng.common.util.FileUtils;
import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.Utils;

import org.conscrypt.Conscrypt;

import java.io.File;
import java.io.IOException;
import java.security.Security;
import java.util.Locale;
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
            DnsOverHttps.Builder dns = new DnsOverHttps.Builder()
                    .client(new OkHttpClient.Builder().build());
            if (BingWallpaperUtils.getLocale(context) == Locale.CHINA) {
                dns.url(HttpUrl.get(Constants.DOH_CHINA));
            } else {
                dns.url(HttpUrl.get(Constants.DOH_CLOUDFLARE));
            }
            builder.dns(dns.build());
        }
        return builder;
    }

    private void ssl() {
        Security.insertProviderAt(Conscrypt.newProvider(), 1);
    }

    public void init(Context context) {
        ssl();
        Retrofit.Builder factory = new Retrofit.Builder().baseUrl(Constants.LOCAL_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create());
        OkHttpClient.Builder simpleBuilder = initOkHttpClientBuilder(context, 60, 30);
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> L.alog().d("NetUtils", message));
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
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
                .flatMap(u -> {
                    try {
                        File temp = GlideApp.with(context)
                                .asFile()
                                .load(u)
                                .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                                .get(2, TimeUnit.MINUTES);
                        L.alog().i("NetUtils", "wallpaper download url: %s", u);
                        return Observable.just(WallpaperUtils.saveToFile(context, u, temp));
                    } catch (Throwable e) {
                        return Observable.error(e);
                    }
                });
        return Utils.addSubscribe(observable, callback);
    }
}
