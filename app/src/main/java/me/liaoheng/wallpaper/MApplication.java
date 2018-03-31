package me.liaoheng.wallpaper;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.github.liaoheng.common.Common;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import me.liaoheng.wallpaper.service.AutoSetWallpaperBroadcastReceiver;
import me.liaoheng.wallpaper.service.BingWallpaperIntentService;
import me.liaoheng.wallpaper.service.ShortcutBroadcastReceiver;
import me.liaoheng.wallpaper.ui.MainActivity;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.Constants;
import me.liaoheng.wallpaper.util.LogDebugFileUtils;
import me.liaoheng.wallpaper.util.NetUtils;
import me.liaoheng.wallpaper.util.TasksUtils;

/**
 * @author liaoheng
 * @version 2016-09-19 11:34
 */
public class MApplication extends Application {

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (!BuildConfig.DEBUG) {
            Crashlytics.log("onTrimMemory level : " + level);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Common.init(this, Constants.PROJECT_NAME, BuildConfig.DEBUG);
        TasksUtils.init(this);
        if (BingWallpaperUtils.isEnableLog(this)) {
            LogDebugFileUtils.get().init();
            LogDebugFileUtils.get().open();
        }
        NetUtils.get().init();

        //debug firebase not work
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(!BuildConfig.DEBUG);

        register();
    }

    /**
     * 动态创建
     */
    @TargetApi(Build.VERSION_CODES.N_MR1)
    public void register() {
        ShortcutManager mShortcutManager = getSystemService(ShortcutManager.class);
        List<ShortcutInfo> infos = new ArrayList<>();

        Intent bothIntent = new Intent(this, AutoSetWallpaperBroadcastReceiver.class);
        bothIntent.setAction(ShortcutBroadcastReceiver.SHORTCUT_SET_WALLPAPER);
        bothIntent.putExtra(BingWallpaperIntentService.EXTRA_SET_WALLPAPER_MODE,
                Constants.EXTRA_SET_WALLPAPER_MODE_BOTH);

        ShortcutInfo bothInfo = new ShortcutInfo.Builder(this, "bothInfo")
                .setShortLabel(getString(R.string.pref_set_wallpaper_auto_mode_both))
                .setLongLabel(getString(R.string.menu_set_wallpaper_mode_both))
                .setIcon(Icon.createWithResource(this, R.drawable.ic_smartphone_white_24dp))
                .setIntent(bothIntent)
                .build();
        infos.add(bothInfo);

        Intent homeIntent = new Intent(this, AutoSetWallpaperBroadcastReceiver.class);
        homeIntent.setAction(ShortcutBroadcastReceiver.SHORTCUT_SET_WALLPAPER);
        homeIntent.putExtra(BingWallpaperIntentService.EXTRA_SET_WALLPAPER_MODE,
                Constants.EXTRA_SET_WALLPAPER_MODE_HOME);

        ShortcutInfo homeInfo = new ShortcutInfo.Builder(this, "homeInfo")
                .setShortLabel(getString(R.string.pref_set_wallpaper_auto_mode_home))
                .setLongLabel(getString(R.string.menu_set_wallpaper_mode_home))
                .setIcon(Icon.createWithResource(this, R.drawable.ic_home_white_24dp))
                .setIntent(homeIntent)
                .build();
        infos.add(homeInfo);

        Intent lockIntent = new Intent(this, AutoSetWallpaperBroadcastReceiver.class);
        lockIntent.setAction(ShortcutBroadcastReceiver.SHORTCUT_SET_WALLPAPER);
        lockIntent.putExtra(BingWallpaperIntentService.EXTRA_SET_WALLPAPER_MODE,
                Constants.EXTRA_SET_WALLPAPER_MODE_LOCK);

        ShortcutInfo lockInfo = new ShortcutInfo.Builder(this, "lockInfo")
                .setShortLabel(getString(R.string.pref_set_wallpaper_auto_mode_lock))
                .setLongLabel(getString(R.string.menu_set_wallpaper_mode_lock))
                .setIcon(Icon.createWithResource(this, R.drawable.ic_lock_white_24dp))
                .setIntent(lockIntent)
                .build();
        infos.add(lockInfo);

        mShortcutManager.setDynamicShortcuts(infos);
    }
}
