package me.liaoheng.bingwallpaper;

import android.app.Application;
import com.github.liaoheng.common.Common;
import java.util.concurrent.TimeUnit;
import jonathanfinerty.once.Once;
import net.danlew.android.joda.JodaTimeAndroid;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * @author liaoheng
 * @version 2016-09-19 11:34
 */
public class App extends Application {
    private static App mInstance = null;
    Retrofit mRetrofit;

    public Retrofit getRetrofit() {
        return mRetrofit;
    }

    public static App getInstance() {
        return mInstance;
    }

    @Override public void onCreate() {
        super.onCreate();
        Common.init(this,"BingWallpaper",BuildConfig.DEBUG);
        Once.initialise(this);
        JodaTimeAndroid.init(this);
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS)
                .build();
        mRetrofit = new Retrofit.Builder().baseUrl("http://www.bing.com").addConverterFactory(JacksonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create()).client(client).build();
        mInstance = this;
    }
}
