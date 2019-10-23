package me.liaoheng.wallpaper.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.NotificationUtils;

/**
 * @author liaoheng
 * @version 2019-10-22 16:50
 */
public class BingWallpaperCheckIntentService extends IntentService {

    public BingWallpaperCheckIntentService() {
        super("BingWallpaperCheckIntentService");
    }

    public static void start(Context context, String tag) {
        Intent intent = new Intent(context, BingWallpaperCheckIntentService.class);
        intent.putExtra("tag", tag);
        ContextCompat.startForegroundService(context, intent);
    }

    @Override
    public void onCreate() {
        setIntentRedelivery(true);
        NotificationUtils.showCheckNotification(this);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }
        BingWallpaperUtils.runningService(getApplicationContext(), intent.getStringExtra("tag"));
    }
}
