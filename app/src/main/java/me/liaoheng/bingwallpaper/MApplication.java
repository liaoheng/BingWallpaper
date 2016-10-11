package me.liaoheng.bingwallpaper;

import android.app.Application;
import com.github.liaoheng.common.Common;
import com.github.liaoheng.common.util.FileUtils;
import com.github.liaoheng.common.util.SystemException;
import java.io.File;
import java.util.concurrent.TimeUnit;
import jonathanfinerty.once.Once;
import me.liaoheng.bingwallpaper.util.Constants;
import me.liaoheng.bingwallpaper.util.LogDebugFileUtils;
import me.liaoheng.bingwallpaper.util.Utils;
import net.danlew.android.joda.JodaTimeAndroid;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * @author liaoheng
 * @version 2016-09-19 11:34
 */
public class MApplication extends Application {
    private static MApplication mInstance = null;
    private Retrofit mRetrofit;

    public Retrofit getRetrofit() {
        return mRetrofit;
    }

    public static MApplication getInstance() {
        return mInstance;
    }

    @Override public void onCreate() {
        super.onCreate();
        Common.init(this, Constants.PROJECT_NAME, BuildConfig.DEBUG);
        Once.initialise(this);
        JodaTimeAndroid.init(this);
        if (Utils.isEnableLog(this)) {
            LogDebugFileUtils.get().init("log.txt");
            LogDebugFileUtils.get().open();
        }


        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS);
        try {
            File cacheFile = FileUtils.createCacheSDAndroidDirectory(Constants.HTTP_CACHE_DIR);
            builder.cache(new Cache(cacheFile, Constants.HTTP_DISK_CACHE_SIZE));
        } catch (SystemException ignored) {
        }
        mRetrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL)
                .addConverterFactory(JacksonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create()).client(builder.build())
                .build();
        mInstance = this;
    }
}
