package me.liaoheng.bingwallpaper.util;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

import com.github.liaoheng.common.util.L;

import java.util.concurrent.TimeUnit;

import me.liaoheng.bingwallpaper.service.JobSchedulerDaemonService;

/**
 * @author liaoheng
 * @version 2017-12-21 15:25
 */
public class BingWallpaperJobManager {
    private static final String TAG = BingWallpaperJobManager.class.getSimpleName();
    private static final int JOB_ID = 0x483;

    public static void disabled(Context context) {
        JobScheduler jobScheduler = (JobScheduler)
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(JOB_ID);
    }

    public static void enabled(Context context) {
        enabled(context, TimeUnit.MINUTES.toMillis(60));
    }

    public static void enabled(Context context, long time) {
        JobScheduler jobScheduler = (JobScheduler)
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo jobInfo = new JobInfo.Builder(JOB_ID, new ComponentName(context,
                JobSchedulerDaemonService.class)).setPeriodic(time)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .build();
        jobScheduler.schedule(jobInfo);

        if (BingWallpaperUtils.isEnableLog(context)) {
            LogDebugFileUtils.get().i(TAG, "job interval time : %s", time / 1000 / 60);
        }
        L.Log.i(TAG, "job interval time : %s", time / 1000 / 60);
    }

}
