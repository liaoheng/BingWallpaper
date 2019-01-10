package me.liaoheng.wallpaper.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import com.bumptech.glide.request.target.Target;
import com.github.liaoheng.common.Common;
import com.github.liaoheng.common.util.*;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import me.liaoheng.wallpaper.data.BingWallpaperNetworkService;
import okhttp3.Cache;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.internal.Util;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.io.FilenameUtils;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
    private Retrofit mSingleRetrofit;

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

    public void init(Context context) {
        Retrofit.Builder factory = new Retrofit.Builder().baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create());

        OkHttpClient.Builder simpleBuilder = new OkHttpClient.Builder().readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS);
        ExecutorService threadPoolExecutor = Executors
                .newSingleThreadExecutor(Util.threadFactory("OkHttp Dispatcher", false));
        Dispatcher dispatcher = new Dispatcher(threadPoolExecutor);
        mSingleRetrofit = factory.client(simpleBuilder.dispatcher(dispatcher).build()).build();

        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(
                message -> {
                    L.alog().d("NetUtils", message);
                });
        httpLoggingInterceptor.setLevel(
                L.isPrint() ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);
        simpleBuilder.dispatcher(new Dispatcher()).addNetworkInterceptor(httpLoggingInterceptor);
        try {
            File cacheFile = FileUtils.getProjectSpaceCacheDirectory(context, Constants.HTTP_CACHE_DIR);
            simpleBuilder.cache(new Cache(cacheFile, Constants.HTTP_DISK_CACHE_SIZE));
        } catch (SystemException ignored) {
        }
        client = simpleBuilder.build();
        mRetrofit = factory.client(client).build();
    }

    private BingWallpaperNetworkService mBingWallpaperNetworkService;
    private BingWallpaperNetworkService mBingWallpaperSingleNetworkService;

    public BingWallpaperNetworkService getBingWallpaperNetworkService() {
        if (mBingWallpaperNetworkService == null) {
            mBingWallpaperNetworkService = mRetrofit.create(BingWallpaperNetworkService.class);
        }
        return mBingWallpaperNetworkService;
    }

    public BingWallpaperNetworkService getBingWallpaperSingleNetworkService() {
        if (mBingWallpaperSingleNetworkService == null) {
            mBingWallpaperSingleNetworkService = mSingleRetrofit.create(BingWallpaperNetworkService.class);
        }
        return mBingWallpaperSingleNetworkService;
    }

    public Disposable downloadImageToFile(final Context context, String url, Callback<File> callback) {
        Observable<File> observable = Observable.just(url).subscribeOn(Schedulers.io())
                .map(url1 -> {
                    File temp = null;
                    try {
                        String name = FilenameUtils.getName(url1);
                        File p = new File(Environment.DIRECTORY_PICTURES, Common.getProjectName());
                        File file = new File(FileUtils.getExternalStoragePath(), p.getAbsolutePath());
                        File outFile = FileUtils.createFile(file, name);
                        temp = GlideApp.with(context)
                                .asFile()
                                .load(url1)
                                .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                                .get(2, TimeUnit.MINUTES);
                        FileUtils.copyFile(temp, outFile);
                        context.sendBroadcast(
                                new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(outFile)));
                        return outFile;
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
