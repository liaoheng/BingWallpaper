package me.liaoheng.wallpaper.util;

import android.support.annotation.NonNull;

import com.github.liaoheng.common.util.FileUtils;
import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.SystemException;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import me.liaoheng.wallpaper.data.BingWallpaperNetworkService;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
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
    private Retrofit mSimpleRetrofit;

    public void init() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder().readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS).addInterceptor(new LogInterceptor("NetUtils"));
        OkHttpClient.Builder simpleBuilder = new OkHttpClient.Builder().readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS);
        try {
            File cacheFile = FileUtils.createCacheSDAndroidDirectory(Constants.HTTP_CACHE_DIR);
            builder.cache(new Cache(cacheFile, Constants.HTTP_DISK_CACHE_SIZE));
        } catch (SystemException ignored) {
        }
        Retrofit.Builder factory = new Retrofit.Builder().baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create());
        mRetrofit = factory.client(builder.build()).build();
        mSimpleRetrofit = factory.client(simpleBuilder.build()).build();
    }

    private BingWallpaperNetworkService mBingWallpaperNetworkService;
    private BingWallpaperNetworkService mBingWallpaperSimpleNetworkService;

    public BingWallpaperNetworkService getBingWallpaperNetworkService() {
        if (mBingWallpaperNetworkService == null) {
            mBingWallpaperNetworkService = mRetrofit.create(BingWallpaperNetworkService.class);
        }
        return mBingWallpaperNetworkService;
    }

    public BingWallpaperNetworkService getBingWallpaperSimpleNetworkService() {
        if (mBingWallpaperSimpleNetworkService == null) {
            mBingWallpaperSimpleNetworkService = mSimpleRetrofit.create(BingWallpaperNetworkService.class);
        }
        return mBingWallpaperSimpleNetworkService;
    }

    /**
     * http log
     *
     * @see <a href="https://github.com/square/okhttp/blob/master/okhttp-logging-interceptor/src/main/java/okhttp3/logging/HttpLoggingInterceptor.java">HttpLoggingInterceptor</a>
     */
    public static class LogInterceptor implements Interceptor {

        private String tag;

        public LogInterceptor() {
            tag = LogInterceptor.class.getSimpleName();
        }

        public LogInterceptor(String tag) {
            this.tag = tag;
        }

        /**
         * Returns true if the body in question probably contains human readable text. Uses a small sample
         * of code points to detect unicode control characters commonly used in binary file signatures.
         */
        private static boolean isPlaintext(Buffer buffer) {
            try {
                Buffer prefix = new Buffer();
                long byteCount = buffer.size() < 64 ? buffer.size() : 64;
                buffer.copyTo(prefix, 0, byteCount);
                for (int i = 0; i < 16; i++) {
                    if (prefix.exhausted()) {
                        break;
                    }
                    int codePoint = prefix.readUtf8CodePoint();
                    if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                        return false;
                    }
                }
                return true;
            } catch (EOFException e) {
                return false; // Truncated UTF-8 sequence.
            }
        }

        @Override
        public Response intercept(@NonNull Chain chain) throws IOException {

            Request request = chain.request();
            long t1 = System.nanoTime();
            L.Log.d(tag, "Sending request %s on %s%n%s", request.url(), request.method(),
                    request.headers());

            try {
                if (request.body() != null) {
                    if (!request.body().contentType()
                            .equals(MediaType.parse("multipart/form-data"))) {
                        final Buffer buffer = new Buffer();
                        request.body().writeTo(buffer);
                        if (isPlaintext(buffer)) {
                            L.Log.d(tag, buffer.clone().readUtf8());
                        }
                    }
                }
            } catch (Exception ignored) {
            }

            Response response = chain.proceed(request);

            long t2 = System.nanoTime();
            L.Log.d(tag, "Received response(%s) for %s in %.1fms%n%s", response.code(),
                    response.request().url(), (t2 - t1) / 1e6d, response.headers());

            try {
                if (response.body() != null) {
                    ResponseBody responseBody = response.body();
                    BufferedSource source = responseBody.source();
                    source.request(Long.MAX_VALUE); // Buffer the entire body.
                    Buffer buffer = source.buffer();
                    if (isPlaintext(buffer)) {
                        L.Log.d(tag, buffer.clone().readUtf8());
                    }
                }
            } catch (Exception ignored) {
            }
            return response;
        }
    }
}
