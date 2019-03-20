package me.liaoheng.wallpaper.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import androidx.annotation.NonNull;
import com.bumptech.glide.request.target.Target;
import com.github.liaoheng.common.Common;
import com.github.liaoheng.common.util.Callback;
import com.github.liaoheng.common.util.*;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import me.liaoheng.wallpaper.data.BingWallpaperNetworkService;
import okhttp3.*;
import okhttp3.internal.Util;
import okhttp3.internal.http.HttpHeaders;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;
import org.apache.commons.io.FilenameUtils;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
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

    /**
     * http log
     *
     * @see <a href="https://github.com/square/okhttp/blob/master/okhttp-logging-interceptor/src/main/java/okhttp3/logging/HttpLoggingInterceptor.java">HttpLoggingInterceptor</a>
     */
    public static class LogInterceptor implements Interceptor {

        private String tag;

        public LogInterceptor() {
            this(LogInterceptor.class.getSimpleName());
        }

        public LogInterceptor(String tag) {
            this.tag = tag;
        }

        private static final Charset UTF8 = Charset.forName("UTF-8");

        /**
         * Returns true if the body in question probably contains human readable text. Uses a small sample
         * of code points to detect unicode control characters commonly used in binary file signatures.
         */
        private boolean isPlaintext(Buffer buffer) {
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

        private static boolean bodyHasUnknownEncoding(Headers headers) {
            String contentEncoding = headers.get("Content-Encoding");
            return contentEncoding != null
                    && !contentEncoding.equalsIgnoreCase("identity")
                    && !contentEncoding.equalsIgnoreCase("gzip");
        }

        @NonNull
        @Override
        public Response intercept(@NonNull Chain chain) throws IOException {
            Request request = chain.request();

            if (!L.isPrint()) {
                return chain.proceed(request);
            }

            RequestBody requestBody = request.body();
            boolean hasRequestBody = requestBody != null;

            Connection connection = chain.connection();
            String requestStartMessage = "--> "
                    + request.method()
                    + ' ' + request.url()
                    + (connection != null ? " " + connection.protocol() : "");
            L.alog().d(tag, requestStartMessage);

            if (hasRequestBody) {
                // Request body headers are only present when installed as a network interceptor. Force
                // them to be included (when available) so there values are known.
                if (requestBody.contentType() != null) {
                    L.alog().d(tag, "Content-Type: " + requestBody.contentType());
                }
                if (requestBody.contentLength() != -1) {
                    L.alog().d(tag, "Content-Length: " + requestBody.contentLength());
                }
            }

            Headers rheaders = request.headers();
            for (int i = 0, count = rheaders.size(); i < count; i++) {
                String name = rheaders.name(i);
                // Skip headers from the request body as they are explicitly logged above.
                if (!"Content-Type".equalsIgnoreCase(name) && !"Content-Length".equalsIgnoreCase(name)) {
                    L.alog().d(tag, rheaders.name(i) + ": " + rheaders.value(i));
                }
            }

            if (!hasRequestBody) {
                L.alog().d(tag, "--> END " + request.method());
            } else if (bodyHasUnknownEncoding(request.headers())) {
                L.alog().d(tag, "--> END " + request.method() + " (encoded body omitted)");
            } else if (requestBody.isDuplex()) {
                L.alog().d(tag, "--> END " + request.method() + " (duplex request body omitted)");
            } else {
                Buffer buffer = new Buffer();
                requestBody.writeTo(buffer);

                Charset charset = UTF8;
                MediaType contentType = requestBody.contentType();
                if (contentType != null) {
                    charset = contentType.charset(UTF8);
                }

                L.alog().d(tag, "");
                if (isPlaintext(buffer)) {
                    L.alog().d(tag, buffer.readString(charset));
                    L.alog().d(tag, "--> END " + request.method()
                            + " (" + requestBody.contentLength() + "-byte body)");
                } else {
                    L.alog().d(tag, "--> END " + request.method() + " (binary "
                            + requestBody.contentLength() + "-byte body omitted)");
                }
            }

            long startNs = System.nanoTime();
            Response response;
            try {
                response = chain.proceed(request);
            } catch (Exception e) {
                L.alog().d(tag, "<-- HTTP FAILED: " + e);
                throw e;
            }
            long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                L.alog().d(tag, "responseBody is null");
                return response;
            }
            long contentLength = responseBody.contentLength();
            String bodySize = contentLength != -1 ? contentLength + "-byte" : "unknown-length";
            L.alog().d(tag, "<-- "
                    + response.code()
                    + (response.message().isEmpty() ? "" : ' ' + response.message())
                    + ' ' + response.request().url()
                    + " (" + tookMs + "ms" + ", " + bodySize + " body" + ')');

            Headers sheaders = response.headers();
            for (int i = 0, count = sheaders.size(); i < count; i++) {
                L.alog().d(tag, sheaders.name(i) + ": " + sheaders.value(i));
            }

            if (!HttpHeaders.hasBody(response)) {
                L.alog().d(tag, "<-- END HTTP");
            } else if (bodyHasUnknownEncoding(response.headers())) {
                L.alog().d(tag, "<-- END HTTP (encoded body omitted)");
            } else {
                BufferedSource source = responseBody.source();
                source.request(Long.MAX_VALUE); // Buffer the entire body.
                Buffer buffer = source.getBuffer();

                Long gzippedLength = null;
                if ("gzip".equalsIgnoreCase(sheaders.get("Content-Encoding"))) {
                    gzippedLength = buffer.size();
                    try (GzipSource gzippedResponseBody = new GzipSource(buffer.clone())) {
                        buffer = new Buffer();
                        buffer.writeAll(gzippedResponseBody);
                    }
                }

                Charset charset = UTF8;
                MediaType contentType = responseBody.contentType();
                if (contentType != null) {
                    charset = contentType.charset(UTF8);
                }

                if (!isPlaintext(buffer)) {
                    L.alog().d(tag, "");
                    L.alog().d(tag, "<-- END HTTP (binary " + buffer.size() + "-byte body omitted)");
                    return response;
                }

                if (contentLength != 0) {
                    L.alog().d(tag, "");
                    L.alog().d(tag, buffer.clone().readString(charset));
                }

                if (gzippedLength != null) {
                    L.alog().d(tag, "<-- END HTTP (" + buffer.size() + "-byte, "
                            + gzippedLength + "-gzipped-byte body)");
                } else {
                    L.alog().d(tag, "<-- END HTTP (" + buffer.size() + "-byte body)");
                }
            }
            return response;
        }
    }
}
