package me.liaoheng.bingwallpaper.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.NetworkUtils;

import me.liaoheng.bingwallpaper.util.LogDebugFileUtils;
import me.liaoheng.bingwallpaper.util.TasksUtils;
import me.liaoheng.bingwallpaper.util.BUtils;


/**
 * @author liaoheng
 * @version 2017-10-16 11:55
 */
public class JobSchedulerService extends JobService {

    private final String TAG = JobSchedulerService.class.getSimpleName();

    Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (NetworkUtils.isConnectedOrConnecting(getApplicationContext())) {
                if (BUtils.getOnlyWifi(getApplicationContext())) {
                    if (!NetworkUtils.isWifiConnected(getApplicationContext())) {
                        L.Log.i(TAG, "isWifiConnected :false");
                        return;
                    }
                }
                //每天成功执行一次
                if (TasksUtils.isToDaysDo(1, BingWallpaperIntentService.FLAG_SET_WALLPAPER_STATE)) {
                    L.Log.i(TAG, "isToDaysDo :true");
                    startService(new Intent(getApplicationContext(), BingWallpaperIntentService.class));
                } else {
                    L.Log.i(TAG, "isToDaysDo :false");
                }
            } else {
                L.Log.i(TAG, "isConnectedOrConnecting :false");
            }
            jobFinished((JobParameters) msg.obj, false);
        }
    };

    @Override
    public boolean onStartJob(JobParameters params) {
        L.Log.i(TAG, "action job id : %s", params.getJobId());
        if (BUtils.isEnableLog(getApplicationContext())) {
            LogDebugFileUtils.get()
                    .i(TAG, "action job id : %s", params.getJobId());
        }
        Message message = new Message();
        message.what = 1;
        message.obj = params;
        mHandler.sendMessageDelayed(message, 3000);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        mHandler.removeMessages(1);
        return false;
    }
}
