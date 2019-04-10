package me.liaoheng.wallpaper.util;

import android.content.Context;
import android.util.Log;
import androidx.work.*;
import com.github.liaoheng.common.util.L;
import com.google.common.util.concurrent.ListenableFuture;
import me.liaoheng.wallpaper.service.BingWallpaperWorker;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author liaoheng
 * @version 2019-03-18 10:42
 */
public class WorkerManager {
    private static final String TAG = WorkerManager.class.getSimpleName();
    private static final String WORKER_TAG = "bing_wallpaper_worker_" + 0x484;

    public static void disabled() {
        WorkManager.getInstance().cancelUniqueWork(WORKER_TAG);
    }

    /**
     * @param time seconds
     */
    public static boolean enabled(Context context, long time) {
        try {

            PeriodicWorkRequest.Builder builder = new PeriodicWorkRequest.Builder(BingWallpaperWorker.class, time,
                    TimeUnit.SECONDS)
                    .addTag(WORKER_TAG);

            WorkManager.getInstance()
                    .enqueueUniquePeriodicWork(WORKER_TAG, ExistingPeriodicWorkPolicy.REPLACE, builder.build());

            BingWallpaperJobManager.setJobType(context, BingWallpaperJobManager.WORKER);
            if (BingWallpaperUtils.isEnableLog(context)) {
                LogDebugFileUtils.get()
                        .i(TAG, "Enable worker interval time : %s", time);
            }
            L.alog().d(TAG, "Enable worker interval time : %s", time);
            return true;
        } catch (Exception e) {
            L.alog().e(TAG, e);
            return false;
        }
    }

    public static void init(Context context, boolean debug) {
        WorkManager.initialize(context,
                new Configuration.Builder().setMinimumLoggingLevel(debug ? Log.DEBUG : Log.ERROR)
                        .setExecutor(Executors.newSingleThreadExecutor())
                        .build());
    }

    public static boolean isScheduled() {
        ListenableFuture<List<WorkInfo>> statuses = WorkManager.getInstance().getWorkInfosForUniqueWork(WORKER_TAG);
        try {
            boolean running = false;
            List<WorkInfo> workInfoList = statuses.get();
            for (WorkInfo workInfo : workInfoList) {
                WorkInfo.State state = workInfo.getState();
                running = state == WorkInfo.State.RUNNING | state == WorkInfo.State.ENQUEUED;
            }
            return running;
        } catch (ExecutionException e) {
            return false;
        } catch (InterruptedException e) {
            return false;
        }
    }
}
