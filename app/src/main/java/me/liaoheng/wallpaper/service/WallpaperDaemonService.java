package me.liaoheng.wallpaper.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.Utils;

import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.Constants;
import me.liaoheng.wallpaper.util.LogDebugFileUtils;
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
        startTime = 0;
        final long time = intent.getLongExtra("time", -1);
        Notification notification = new NotificationCompat.Builder(getApplicationContext(),
                Constants.FOREGROUND_DAEMON_SERVICE_NOTIFICATION_CHANNEL).setPriority(
                NotificationCompat.PRIORITY_MIN)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .setSmallIcon(
                        R.drawable.ic_notification)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.daemon_service_running))
                .build();
        startForeground(0x112, notification);

        action = Observable.interval(0, 2, TimeUnit.MINUTES)
                .subscribe(new Consumer<Long>() {

                    @Override
                    public void accept(Long aLong) throws Exception {
                        startTime += TimeUnit.MINUTES.toSeconds(2);
                        L.alog().d(TAG, " running...  " + startTime);
                        if (startTime >= time) {
                            startTime = 0;
                            if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
                                LogDebugFileUtils.get().i(TAG, "daemon service action");
                            }
                            //每天成功执行一次
                            if (TasksUtils.isToDaysDoProvider(getApplicationContext(), 1,
                                    BingWallpaperIntentService.FLAG_SET_WALLPAPER_STATE)) {
                                SetWallpaperBroadcastReceiver.send(getApplicationContext(), TAG);
                            }
                        }
                    }
                });
        return START_REDELIVER_INTENT;
    }
}
