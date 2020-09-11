package me.liaoheng.wallpaper.util;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;
import com.github.liaoheng.common.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import me.liaoheng.wallpaper.BuildConfig;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * @author liaoheng
 * @version 2016-09-22 10:13
 */
@GlideModule
public class MGlideModule extends AppGlideModule {

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        try {
            File imgCache = FileUtils.getProjectSpaceCacheDirectory(context, Constants.DISK_CACHE_DIR);
            builder.setDiskCache(new DiskLruCacheFactory(imgCache.getAbsolutePath(),
                    Constants.IMAGE_DISK_CACHE_SIZE));
        } catch (IOException ignored) {
        }
        builder.setLogLevel(BuildConfig.DEBUG ? Log.DEBUG : Log.INFO);
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        OkHttpClient.Builder builder = NetUtils.get().initOkHttpClientBuilder(context, 120, 60);
        builder.addInterceptor(chain -> {
            Request request = chain.request()
                    .newBuilder()
                    .header("User-Agent", Constants.USER_AGENT)
                    .header("Accept","image/avif,image/webp,image/apng,image/*,*/*;q=0.8")
                    .build();
            return chain.proceed(request);
        });
        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(builder.build()));
    }
}