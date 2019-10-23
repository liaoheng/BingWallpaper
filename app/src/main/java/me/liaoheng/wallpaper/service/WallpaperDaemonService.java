package me.liaoheng.wallpaper.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.Utils;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.Constants;
import me.liaoheng.wallpaper.util.LogDebugFileUtils;
import me.liaoheng.wallpaper.util.NotificationUtils;
import me.liaoheng.wallpaper.util.TasksUtils;

/**
 * 守护进程服务
 *
 * @author liaoheng
 * @version 2018-07-06 13:35
 */
public class WallpaperDaemonService extends Service {
    public final String TAG = WallpaperDaemonService.class.getSimpleName();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Disposable action;
    private long startTime;

    @Override
    public void onDestroy() {
        Utils.dispose(action);
        action = null;
        stopForeground(true);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        if (action != null) {
            return START_REDELIVER_INTENT;
        }
        NotificationUtils.showRunningNotification(this);
        startTime = 0;
        final long time = intent.getLongExtra("time", Constants.DAEMON_SERVICE_PERIODIC);

        action = Observable.interval(0, 2, TimeUnit.MINUTES)
                .subscribe(aLong -> {
                    startTime += TimeUnit.MINUTES.toSeconds(2);
                    L.alog().d(TAG, " running...  " + startTime);
                    if (startTime >= time) {
                        startTime = 0;
                        if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
                            LogDebugFileUtils.get().i(TAG, "daemon service action : %s", startTime);
                        }
                        if (TasksUtils.isToDaysDoProvider(getApplicationContext(), 1,
                                BingWallpaperIntentService.FLAG_SET_WALLPAPER_STATE)) {
                            BingWallpaperUtils.startCheckService(getApplicationContext(), TAG);
                        }
                    }
                });
        return START_REDELIVER_INTENT;
    }
}
