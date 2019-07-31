package me.liaoheng.wallpaper.util;

import android.content.Context;
import com.github.liaoheng.common.BuildConfig;
import com.github.liaoheng.common.cache.DiskLruCache;
import com.github.liaoheng.common.util.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;

/**
 * @author liaoheng
 * @version 2018-12-28 15:11
 */
public class CacheUtils {
    private static CacheUtils mCacheUtils;

    public static CacheUtils get() {
        if (mCacheUtils == null) {
            mCacheUtils = new CacheUtils();
        }
        return mCacheUtils;
    }

    private DiskLruCache diskLruCache;

    public void init(Context context) {
        try {
            init(FileUtils.getProjectSpaceCacheDirectory(context, "temp"));
        } catch (Exception ignored) {
        }
    }

    public void init(File cachePath) throws IOException {
        diskLruCache = DiskLruCache.open(cachePath, BuildConfig.VERSION_CODE, 1,
                1024 * 1024 * 10);//Bytes
    }

    public void clear(Context context){
        try {
            diskLruCache.delete();
            init(context);
        } catch (IOException ignored) {
        }
    }

    public File put(String key, File file) {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            return put(key, inputStream);
        } catch (IOException ignored) {
        }
        return null;
    }

    public File put(int key, InputStream inputStream) {
        return put(String.valueOf(key), inputStream);
    }

    public File put(String key, InputStream inputStream) {
        DiskLruCache.Editor edit = null;
        OutputStream outputStream = null;
        try {
            edit = diskLruCache.edit(key);
            outputStream = edit.newOutputStream(0);
            IOUtils.copy(inputStream, outputStream);
            edit.commit();
            diskLruCache.flush();
        } catch (IOException e) {
            try {
                if (edit != null) {
                    edit.abort();
                }
            } catch (IOException ignored) {
            }
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
        if (edit == null || edit.isHasErrors()) {
            return null;
        }
        return edit.getEntry().getCleanFile(0);
    }

    public File get(String key) {
        try {
            return diskLruCache.getFile(key, 0);
        } catch (Exception ignored) {
        }
        return null;
    }
}
