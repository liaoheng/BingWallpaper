package me.liaoheng.wallpaper.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import me.liaoheng.wallpaper.model.Config;
import me.liaoheng.wallpaper.model.Wallpaper;
import me.liaoheng.wallpaper.util.NotificationUtils;

/**
 * 设置壁纸操作IntentService
 *
 * @author liaoheng
 * @version 2016-9-19 12:48
 */
public class BingWallpaperIntentService extends IntentService {
    private final String TAG = BingWallpaperIntentService.class.getSimpleName();
    private SetWallpaperDelegate mSetWallpaperDelegate;

    public BingWallpaperIntentService() {
        super("BingWallpaperIntentService");
    }

    public static void start(Context context, @Nullable Wallpaper image,
            @NonNull Config config) {
        Intent intent = new Intent(context, BingWallpaperIntentService.class);
        intent.putExtra(Config.EXTRA_SET_WALLPAPER_IMAGE, image);
        intent.putExtra(Config.EXTRA_SET_WALLPAPER_CONFIG, config);
        start(context, intent);
    }

    public static void start(Context context, Intent intent) {
        ContextCompat.startForegroundService(context, intent);
    }

    @Override
    public void onCreate() {
        mSetWallpaperDelegate = new SetWallpaperDelegate(this, TAG);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }

    //https://issuetracker.google.com/issues/76112072
    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        NotificationUtils.showStartNotification(this);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mSetWallpaperDelegate.setWallpaper(intent);
    }
}
