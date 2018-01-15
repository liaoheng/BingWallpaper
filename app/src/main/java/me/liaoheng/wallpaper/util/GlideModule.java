package me.liaoheng.wallpaper.util;

import android.content.Context;
import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;
import com.github.liaoheng.common.util.FileUtils;
import com.github.liaoheng.common.util.SystemException;
import java.io.File;

/**
 * @author liaoheng
 * @version 2016-09-22 10:13
 */
public class GlideModule implements com.bumptech.glide.module.GlideModule {

    @Override public void applyOptions(Context context, GlideBuilder builder) {
        try {
            File imgCache = FileUtils.createCacheSDAndroidDirectory(Constants.DISK_CACHE_DIR);
            builder.setDiskCache(new DiskLruCacheFactory(imgCache.getAbsolutePath(),
                    Constants.IMAGE_DISK_CACHE_SIZE));
        } catch (SystemException ignored) {
        }
    }

    @Override public void registerComponents(Context context, Glide glide) {
    }
}