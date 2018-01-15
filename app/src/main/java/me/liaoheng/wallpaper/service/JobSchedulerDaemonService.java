package me.liaoheng.wallpaper.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.NetworkUtils;

import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.LogDebugFileUtils;
import me.liaoheng.wallpaper.util.TasksUtils;


/**
 * 自动更新壁纸守护服务
 *
 * @author liaoheng
 * @version 2017-10-16 11:55
 */
public class JobSchedulerDaemonService extends JobService {

    private final String TAG = JobSchedulerDaemonService.class.getSimpleName();

    Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (BingWallpaperUtils.isConnectedOrConnecting(getApplicationContext())) {
                if (BingWallpaperUtils.getOnlyWifi(getApplicationContext())) {
                    if (!NetworkUtils.isWifiConnected(getApplicationContext())) {
                        L.Log.i(TAG, "isWifiConnected :false");
                        return;
                    }
                }
                //每天成功执行一次
                if (TasksUtils.isToDaysDo(1, BingWallpaperIntentService.FLAG_SET_WALLPAPER_STATE)) {
                    L.Log.i(TAG, "isToDaysDo :true");
                    Intent intent1 = new Intent(getApplicationContext(), BingWallpaperIntentService.class);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(intent1);
                    } else {
                        startService(intent1);
                    }
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
        if (BingWallpaperUtils.isEnableLog(getApplicationContext())) {
            LogDebugFileUtils.get()
                    .i(TAG, "action job id : %s", params.getJobId());
        }
        Message message = new Message();
        message.what = 1;
        message.obj = params;
        mHandler.sendMessageDelayed(message, 2000);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        mHandler.removeMessages(1);
        return false;
    }
}
