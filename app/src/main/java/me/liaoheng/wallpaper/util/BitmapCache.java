/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.liaoheng.wallpaper.util;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LruCache;
import androidx.core.graphics.BitmapCompat;

/**
 * @author liaoheng
 * @date 2021-08-26 13:37
 */
public class BitmapCache {
    private static final int CACHE_SIZE =
            (int) Math.min(Runtime.getRuntime().maxMemory() / 8, Integer.MAX_VALUE / 4);
    private final MemoryCache mMemoryCache = new MemoryCache(CACHE_SIZE);

    public void put(@NonNull String key, @NonNull Bitmap bitmap) {
        mMemoryCache.put(key, bitmap);
    }

    @Nullable
    public Bitmap get(@NonNull String key) {
        return mMemoryCache.get(key);
    }

    public void clear() {
        mMemoryCache.evictAll();
    }

    private static class MemoryCache extends LruCache<String, Bitmap> {
        private MemoryCache(int maxSize) {
            super(maxSize);
        }

        @Override
        protected int sizeOf(@NonNull String key, @NonNull Bitmap bitmap) {
            return BitmapCompat.getAllocationByteCount(bitmap);
        }
    }
}
