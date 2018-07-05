package me.liaoheng.wallpaper.util;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.github.liaoheng.common.util.L;

import java.util.List;
import java.util.concurrent.TimeUnit;

import me.liaoheng.wallpaper.service.FirebaseJobSchedulerDaemonService;
import me.liaoheng.wallpaper.service.JobSchedulerDaemonService;

/**
 * @author liaoheng
 * @version 2017-12-21 15:25
 */
public class BingWallpaperJobManager {
    private static final String TAG = BingWallpaperJobManager.class.getSimpleName();
    private static final int JOB_ID = 0x483;
    private static final String JOB_TAG = "bing_wallpaper_job_" + JOB_ID;

    public static void disabled(Context context) {
        setJobType(context, -1);
        if (BingWallpaperUtils.isGooglePlayServicesAvailable(context)) {
            FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
            dispatcher.cancel(JOB_TAG);
        }
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

    public static boolean enableGooglePlay(Context context, long time) {
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        Job myJob = dispatcher.newJobBuilder()
                .setService(FirebaseJobSchedulerDaemonService.class)
                .setTag(JOB_TAG)
                .setRecurring(true)
                .setReplaceCurrent(true)
                .setRetryStrategy(
                        RetryStrategy.DEFAULT_EXPONENTIAL)
                .setTrigger(Trigger.executionWindow(0,
                        (int) TimeUnit.MILLISECONDS.toSeconds(time)))
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .build();
        boolean success = dispatcher.schedule(myJob) == FirebaseJobDispatcher.SCHEDULE_RESULT_SUCCESS;
        if (success) {
            setJobType(context, 1);
        }
        return success;
    }

    public static void enableSystem(Context context, long time) {
        JobScheduler jobScheduler = (JobScheduler)
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo jobInfo = new JobInfo.Builder(JOB_ID, new ComponentName(context,
                JobSchedulerDaemonService.class)).setPeriodic(time)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setBackoffCriteria(TimeUnit.MINUTES.toMillis(15), JobInfo.BACKOFF_POLICY_EXPONENTIAL)
                .setPersisted(true)
                .build();
        if (jobScheduler == null) {
            return;
        }
        jobScheduler.schedule(jobInfo);
        setJobType(context, 0);
    }

    public static void enabled(Context context, long time) {
        if (BingWallpaperUtils.getAutomaticUpdateType(context) == 0) {
            if (BingWallpaperUtils.isGooglePlayServicesAvailable(context)) {
                if (!enableGooglePlay(context, time)) {
                    enableSystem(context, time);
                }
            } else {
                enableSystem(context, time);
            }
        } else {
            enableSystem(context, time);
        }
        if (BingWallpaperUtils.isEnableLog(context)) {
            LogDebugFileUtils.get().i(TAG, "job interval time : %s", time / 1000 / 60);
        }
        L.alog().d(TAG, "job interval time : %s", time / 1000 / 60);
    }

    public static void setJobType(Context context, int type) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        sharedPreferences.edit().putInt("bing_wallpaper_job_type", type).apply();
    }

    public static int getJobType(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sharedPreferences.getInt("bing_wallpaper_job_type", -1);
    }

    public static String check(Context context) {
        if (getJobType(context) == 1) {
            if (BingWallpaperUtils.isGooglePlayServicesAvailable(context)) {
                return "play";
            } else {
                return "play error";
            }
        } else {
            JobScheduler jobScheduler = (JobScheduler)
                    context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            if (jobScheduler == null) {
                return "false";
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
            return myJobInfo != null ? "true" : "false";
        }
    }

}
