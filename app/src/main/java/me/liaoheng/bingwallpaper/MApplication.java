package me.liaoheng.bingwallpaper;

import android.app.Application;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

import com.github.liaoheng.common.Common;
import com.github.liaoheng.common.util.L;

import me.liaoheng.bingwallpaper.service.JobSchedulerService;
import me.liaoheng.bingwallpaper.util.Constants;
import me.liaoheng.bingwallpaper.util.LogDebugFileUtils;
import me.liaoheng.bingwallpaper.util.NetUtils;
import me.liaoheng.bingwallpaper.util.TasksUtils;
import me.liaoheng.bingwallpaper.util.Utils;

/**
 * @author liaoheng
 * @version 2016-09-19 11:34
 */
public class MApplication extends Application {
    private static final int JOB_ID = 0x483;

    @Override
    public void onCreate() {
        super.onCreate();
        Common.init(this, Constants.PROJECT_NAME, BuildConfig.DEBUG);
        TasksUtils.init(this);
        if (Utils.isEnableLog(this)) {
            LogDebugFileUtils.get().init("log.txt");
            LogDebugFileUtils.get().open();
        }
        NetUtils.get().init();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobScheduler mJobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            mJobScheduler.cancel(JOB_ID);
            L.Log.d("App ","JOB_ID :"+JOB_ID);

            JobInfo.Builder builder = new JobInfo.Builder(JOB_ID,
                    new ComponentName(this, JobSchedulerService.class));

            builder.setPeriodic(60 * 1000); //每隔60秒运行一次
            //builder.setRequiresCharging(true);
            builder.setPersisted(true); //设置设备重启后，是否重新执行任务
            //builder.setRequiresDeviceIdle(true);

            int schedule = mJobScheduler.schedule(builder.build());
            L.Log.d("App ","  ssss : "+schedule);
            if (schedule <= 0) {
                L.Log.d("App ","JobSchedulerService start error");
            }
        }

    }
}
