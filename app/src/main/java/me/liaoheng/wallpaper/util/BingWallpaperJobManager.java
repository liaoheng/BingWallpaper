package me.liaoheng.wallpaper.util;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.UIUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import me.liaoheng.wallpaper.service.FirebaseJobSchedulerDaemonService;
import me.liaoheng.wallpaper.service.JobSchedulerDaemonService;
import me.liaoheng.wallpaper.service.WallpaperDaemonService;

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

        Intent intent = new Intent(context, WallpaperDaemonService.class);
        context.stopService(intent);

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

    public static void startDaemonService(Context context) {
        Intent intent = new Intent(context, WallpaperDaemonService.class);
        intent.putExtra("time", Constants.JOB_SCHEDULER_PERIODIC);
        ContextCompat.startForegroundService(context, intent);
    }

    public static void startDaemonService(Context context, long time) {
        Intent intent = new Intent(context, WallpaperDaemonService.class);
        intent.putExtra("time", time);
        ContextCompat.startForegroundService(context, intent);
    }

    public static void enableDaemonService(Context context, long time) {
        startDaemonService(context, time);
        setJobType(context, 2);
        if (BingWallpaperUtils.isEnableLog(context)) {
            LogDebugFileUtils.get()
                    .i(TAG, "Enable daemon service interval time : %s", time);
        }
        L.alog().d(TAG, "Enable daemon service interval time : %s", time);
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
                .setTrigger(Trigger.executionWindow(0, (int) time))
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .build();
        boolean success = dispatcher.schedule(myJob) == FirebaseJobDispatcher.SCHEDULE_RESULT_SUCCESS;
        if (success) {
            setJobType(context, 1);
            if (BingWallpaperUtils.isEnableLog(context)) {
                LogDebugFileUtils.get()
                        .i(TAG, "Enable GooglePlay job interval time : %s", time);
            }
            L.alog().d(TAG, "Enable GooglePlay job interval time : %s", time);
        }
        return success;
    }

    public static boolean enableSystem(Context context, long time) {
        JobScheduler jobScheduler = (JobScheduler)
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo jobInfo = new JobInfo.Builder(JOB_ID, new ComponentName(context,
                JobSchedulerDaemonService.class)).setPeriodic(TimeUnit.MILLISECONDS.toSeconds(time))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setBackoffCriteria(TimeUnit.MINUTES.toMillis(15), JobInfo.BACKOFF_POLICY_EXPONENTIAL)
                .setPersisted(true)
                .build();
        if (jobScheduler == null) {
            return false;
        }
        boolean success = jobScheduler.schedule(jobInfo) == JobScheduler.RESULT_SUCCESS;
        if (success) {
            setJobType(context, 0);
            if (BingWallpaperUtils.isEnableLog(context)) {
                LogDebugFileUtils.get()
                        .i(TAG, "Enable system job interval time : %s", time);
            }
            L.alog().d(TAG, "Enable system job interval time : %s", time);
        }
        return success;
    }

    public static void enabled(Context context, long time) {
        int type = BingWallpaperUtils.getAutomaticUpdateType(context);
        if (type == 0) {
            if (BingWallpaperUtils.isGooglePlayServicesAvailable(context)) {
                if (!enableGooglePlay(context, time)) {
                    if (!enableSystem(context, time)) {
                        enableDaemonService(context, time);
                    }
                }
            } else {
                if (!enableSystem(context, time)) {
                    enableDaemonService(context, time);
                }
            }
        } else if (type == 1) {
            if (!enableSystem(context, time)) {
                UIUtils.showToast(context, "set auto set wallpaper service failure");
            }
        } else if (type == 2) {
            enableDaemonService(context, time);
        }
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
        int jobType = getJobType(context);
        if (jobType == 2) {
            return "Daemon Service";
        } else if (jobType == 1) {
            if (BingWallpaperUtils.isGooglePlayServicesAvailable(context)) {
                return "GooglePlay";
            } else {
                return "GooglePlay Error";
            }
        } else {
            JobScheduler jobScheduler = (JobScheduler)
                    context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            if (jobScheduler == null) {
                return "False";
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
            return myJobInfo != null ? "True" : "False";
        }
    }

}
