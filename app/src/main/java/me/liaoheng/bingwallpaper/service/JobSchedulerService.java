package me.liaoheng.bingwallpaper.service;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;

import com.github.liaoheng.common.util.L;

/**
 * @author liaoheng
 * @version 2017-10-16 11:55
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class JobSchedulerService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        L.Log.i("JobSchedulerService", "Today for the first time");

        //if (TasksUtils.isToDaysDo(1, BingWallpaperIntentService.FLAG_SET_WALLPAPER_STATE)) {
        //
        //    if (Utils.isEnableLog(getApplicationContext())) {
        //        LogDebugFileUtils.get()
        //                .i("JobSchedulerService", "Today for the first time");
        //    }
        //    getApplicationContext().startService(new Intent(getApplicationContext(), BingWallpaperIntentService.class));
        //}
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
