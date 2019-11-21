package me.liaoheng.wallpaper.service;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.github.liaoheng.common.util.L;

import org.jetbrains.annotations.NotNull;

import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.LogDebugFileUtils;

/**
 * 自动更新壁纸守护服务
 *
 * @author liaoheng
 * @version 2017-10-16 11:55
 */
public class FirebaseJobSchedulerDaemonService extends JobService {
    private final String TAG = FirebaseJobSchedulerDaemonService.class.getSimpleName();

    private HandlerThread mHandlerThread = new HandlerThread(TAG);
    private Handler mHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(@NotNull Message msg) {
                BingWallpaperUtils.runningService(getApplicationContext(), TAG);
                jobFinished((JobParameters) msg.obj, false);
            }
        };
    }

    @Override
    public boolean onStartJob(@NotNull JobParameters params) {
        L.alog().d(TAG, "action job tag : %s", params.getTag());
        if (BingWallpaperUtils.isEnableLog(getApplicationContext())) {
            LogDebugFileUtils.get()
                    .i(TAG, "action job tag : %s", params.getTag());
        }
        Message message = new Message();
        message.what = 1;
        message.obj = params;
        mHandler.sendMessageDelayed(message, 2000);
        return true;
    }

    @Override
    public boolean onStopJob(@NotNull JobParameters params) {
        mHandler.removeMessages(1);
        mHandlerThread.quit();
        return false;
    }
}
