package me.liaoheng.wallpaper.service;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.github.liaoheng.common.util.L;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.LogDebugFileUtils;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author liaoheng
 * @version 2019-03-18 10:42
 */
public class BingWallpaperWorker extends Worker {
    private final String TAG = BingWallpaperWorker.class.getSimpleName();

    public BingWallpaperWorker(@NonNull Context appContext,
            @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    private AtomicBoolean isRunning = new AtomicBoolean(false);

    @NonNull
    @Override
    public ListenableWorker.Result doWork() {
        L.alog().d(TAG, "action worker id : %s", getId());
        if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
            LogDebugFileUtils.get()
                    .i(TAG, "action worker id : %s", getId());
        }
        if (isRunning.get()) {
            return Result.success();
        }
        isRunning.set(true);
        BingWallpaperUtils.runningService(getApplicationContext(), TAG);
        isRunning.set(false);
        return Result.success();
    }
}
