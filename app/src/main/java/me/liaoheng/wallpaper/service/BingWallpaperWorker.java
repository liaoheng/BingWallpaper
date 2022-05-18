package me.liaoheng.wallpaper.service;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.github.liaoheng.common.util.L;

import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.LogDebugFileUtils;
import me.liaoheng.wallpaper.util.Settings;

/**
 * @author liaoheng
 * @version 2019-03-18 10:42
 */
public class BingWallpaperWorker extends Worker {
    private final String TAG = BingWallpaperWorker.class.getSimpleName();
    private final SetWallpaperDelegate mSetWallpaperDelegate;

    public BingWallpaperWorker(@NonNull Context appContext,
            @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
        mSetWallpaperDelegate = new SetWallpaperDelegate(appContext, TAG);
    }

    @NonNull
    @Override
    public ListenableWorker.Result doWork() {
        L.alog().d(TAG, "action worker id : %s", getId());
        if (Settings.isEnableLogProvider(getApplicationContext())) {
            LogDebugFileUtils.get()
                    .i(TAG, "action worker id : %s", getId());
        }
        Intent intent = BingWallpaperUtils.checkRunningServiceIntent(getApplicationContext(), TAG);
        if (intent == null) {
            return Result.success();
        }
        mSetWallpaperDelegate.setWallpaper(intent);
        return Result.success();
    }
}
