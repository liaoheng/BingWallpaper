package me.liaoheng.wallpaper.util;

import android.content.Context;
import android.support.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.GlideModule;
import com.github.liaoheng.common.util.FileUtils;
import com.github.liaoheng.common.util.SystemException;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * @author liaoheng
 * @version 2016-09-22 10:13
 */
public class MGlideModule implements GlideModule {

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        try {
            File imgCache = FileUtils.createCacheSDAndroidDirectory(Constants.DISK_CACHE_DIR);
            builder.setDiskCache(new DiskLruCacheFactory(imgCache.getAbsolutePath(),
                    Constants.IMAGE_DISK_CACHE_SIZE));
        } catch (SystemException ignored) {
        }
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide) {
        final OkHttpClient.Builder builder = new OkHttpClient.Builder();

        builder.readTimeout(80, TimeUnit.SECONDS);
        builder.connectTimeout(60, TimeUnit.SECONDS);

        glide.register(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(builder.build()));
    }
}