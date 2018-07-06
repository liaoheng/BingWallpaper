package me.liaoheng.wallpaper.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.github.liaoheng.common.util.L;

import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.LogDebugFileUtils;

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
            SetWallpaperBroadcastReceiver.send(getApplicationContext(), TAG);
            jobFinished((JobParameters) msg.obj, false);
        }
    };

    @Override
    public boolean onStartJob(JobParameters params) {
        L.alog().d(TAG, "action job id : %s", params.getJobId());
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
