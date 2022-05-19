package me.liaoheng.wallpaper.service;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.github.liaoheng.common.util.Callback;
import com.github.liaoheng.common.util.L;

import java.io.File;
import java.io.IOException;

import me.liaoheng.wallpaper.data.BingWallpaperNetworkClient;
import me.liaoheng.wallpaper.model.Config;
import me.liaoheng.wallpaper.model.Wallpaper;
import me.liaoheng.wallpaper.util.IUIHelper;
import me.liaoheng.wallpaper.util.UIHelper;
import me.liaoheng.wallpaper.util.WallpaperUtils;

/**
 * @author liaoheng
 * @date 2022-05-18 21:47
 */
public class SetWallpaperDelegate {
    private final String TAG;
    private final Context mContext;
    private final IUIHelper mUiHelper;
    private final SetWallpaperServiceHelper mServiceHelper;

    public SetWallpaperDelegate(Context context, String tag) {
        TAG = tag;
        mContext = context;
        mUiHelper = new UIHelper();
        mServiceHelper = new SetWallpaperServiceHelper(context, TAG);
    }

    public void setWallpaper(Intent intent) {
        if (intent == null) {
            return;
        }
        Wallpaper image = intent.getParcelableExtra(Config.EXTRA_SET_WALLPAPER_IMAGE);
        Config config = intent.getParcelableExtra(Config.EXTRA_SET_WALLPAPER_CONFIG);
        setWallpaper(image, config);
    }

    public void setWallpaper(Wallpaper image, Config config) {
        if (config == null) {
            return;
        }
        L.alog().d(TAG, config.toString());

        Callback<Wallpaper> callback = new Callback.EmptyCallback<Wallpaper>() {
            @Override
            public void onSuccess(Wallpaper bingWallpaperImage) {
                success(config, bingWallpaperImage);
            }

            @Override
            public void onError(Throwable e) {
                failure(config, e);
            }
        };

        mServiceHelper.begin(config);

        if (image == null) {
            try {
                image = BingWallpaperNetworkClient.getWallpaper(getContext(), false);
                image.setResolutionImageUrl(getContext());
            } catch (IOException e) {
                callback.onError(e);
                return;
            }
        } else {
            if (TextUtils.isEmpty(image.getImageUrl())) {
                image.setResolutionImageUrl(getContext());
            }
        }

        try {
            downloadAndSetWallpaper(image, config);
            callback.onSuccess(image);
        } catch (Throwable e) {
            callback.onError(e);
        }
    }

    private void failure(Config config, Throwable throwable) {
        mServiceHelper.failure(config, throwable);
    }

    private void success(Config config, Wallpaper image) {
        mServiceHelper.success(config, image);
    }

    private void downloadAndSetWallpaper(Wallpaper image, Config config)
            throws Throwable {
        File wallpaper = WallpaperUtils.getImageFile(getContext(), image.getImageUrl());

        if (wallpaper == null || !wallpaper.exists()) {
            throw new IOException("Download wallpaper failure");
        }

        if (config.isBackground()) {
            WallpaperUtils.autoSaveWallpaper(getContext(), TAG, image, wallpaper);
        }
        mUiHelper.setWallpaper(getContext(), config, wallpaper, image.getImageUrl());
    }

    private Context getContext() {
        return mContext;
    }
}
