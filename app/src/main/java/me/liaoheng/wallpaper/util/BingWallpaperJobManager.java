package me.liaoheng.wallpaper.util;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

import com.github.liaoheng.common.util.L;

import java.util.List;
import java.util.concurrent.TimeUnit;

import me.liaoheng.wallpaper.service.JobSchedulerDaemonService;

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
        if (jobScheduler == null) {
            return;
        }
        jobScheduler.cancel(JOB_ID);
    }

    public static void enabled(Context context) {
        disabled(context);
        enabled(context, Constants.JOB_SCHEDULER_PERIODIC);
    }

    public static void enabled(Context context, long time) {
        JobScheduler jobScheduler = (JobScheduler)
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo jobInfo = new JobInfo.Builder(JOB_ID, new ComponentName(context,
                JobSchedulerDaemonService.class)).setPeriodic(time)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setBackoffCriteria(TimeUnit.MINUTES.toMillis(15), JobInfo.BACKOFF_POLICY_LINEAR)
                .setPersisted(true)
                .build();
        if (jobScheduler == null) {
            return;
        }
        jobScheduler.schedule(jobInfo);

        if (BingWallpaperUtils.isEnableLog(context)) {
            LogDebugFileUtils.get().i(TAG, "job interval time : %s", time / 1000 / 60);
        }
        L.Log.d(TAG, "job interval time : %s", time / 1000 / 60);
    }

    public static boolean check(Context context) {
        JobScheduler jobScheduler = (JobScheduler)
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler == null) {
            return false;
        }
        JobInfo myJobInfo = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            myJobInfo = jobScheduler.getPendingJob(JOB_ID);
        } else {
            List<JobInfo> allPendingJobs = jobScheduler.getAllPendingJobs();
            for (JobInfo allPendingJob : allPendingJobs) {
                if (allPendingJob.getId() == JOB_ID) {
                    myJobInfo = allPendingJob;
                }
            }
        }
        return myJobInfo == null;
    }

}
