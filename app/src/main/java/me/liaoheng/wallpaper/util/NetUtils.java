package me.liaoheng.wallpaper.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.bumptech.glide.request.target.Target;
import com.github.liaoheng.common.Common;
import com.github.liaoheng.common.util.Callback;
import com.github.liaoheng.common.util.FileUtils;
import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.SystemException;
import com.github.liaoheng.common.util.SystemRuntimeException;
import com.github.liaoheng.common.util.Utils;

import org.apache.commons.io.FilenameUtils;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import me.liaoheng.wallpaper.data.BingWallpaperNetworkService;
import okhttp3.Cache;
import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;
import okio.Buffer;
import okio.BufferedSource;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscription;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

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
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create());

        OkHttpClient.Builder simpleBuilder = new OkHttpClient.Builder().readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS);
        ExecutorService threadPoolExecutor = Executors
                .newSingleThreadExecutor(Util.threadFactory("OkHttp Dispatcher", false));
        Dispatcher dispatcher = new Dispatcher(threadPoolExecutor);
        mSingleRetrofit = factory.client(simpleBuilder.dispatcher(dispatcher).build()).build();

        simpleBuilder.dispatcher(new Dispatcher()).addInterceptor(new LogInterceptor("NetUtils"));
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
                        if (L.isPrint()) {
                            Log.d(tag, buffer.clone().readUtf8());
                        }
                    }
                }
            } catch (Exception ignored) {
            }
            return response;
        }
    }

    public Subscription downloadImageToFile(final Context context, String url, Callback<File> callback) {
        Observable<File> observable = Observable.just(url).subscribeOn(Schedulers.io())
                .map(new Func1<String, File>() {
                    @Override
                    public File call(String url) {
                        File temp = null;
                        try {
                            String name = FilenameUtils.getName(url);
                            File p = new File(Environment.DIRECTORY_PICTURES, Common.getProjectName());
                            File file = new File(FileUtils.getExternalStoragePath(), p.getAbsolutePath());
                            File outFile = FileUtils.createFile(file, name);
                            temp = GlideApp.with(context)
                                    .asFile()
                                    .load(url)
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
                    }
                });
        return Utils.addSubscribe(observable, callback);
    }

}
