package me.liaoheng.wallpaper.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.github.liaoheng.common.util.Utils;

import java.util.concurrent.TimeUnit;

import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.util.Constants;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;

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

    private Subscription action;

    @Override
    public void onDestroy() {
        Utils.unsubscribe(action);
        action = null;
        stopForeground(true);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (action != null) {
            return super.onStartCommand(intent, flags, startId);
        }

        long time = intent.getLongExtra("time", -1);
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

        action = Observable.interval(0, time, TimeUnit.SECONDS)
                .subscribe(new Action1<Long>() {

                    @Override
                    public void call(Long aLong) {
                        SetWallpaperBroadcastReceiver.send(getApplicationContext(), TAG);
                    }
                });
        return super.onStartCommand(intent, flags, startId);
    }
}
