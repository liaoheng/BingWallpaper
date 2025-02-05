package me.liaoheng.wallpaper.util;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.YNCallback;

import org.joda.time.LocalTime;

import java.util.concurrent.TimeUnit;

import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.service.LiveWallpaperService;

/**
 * @author liaoheng
 * @version 2017-12-21 15:25
 */
public class BingWallpaperJobManager {
    private static final String TAG = BingWallpaperJobManager.class.getSimpleName();

    public static void disabled(Context context) {
        disabled(context, false);
    }

    public static void disabled(Context context, boolean force) {
        new Thread(() -> {
            WorkerManager.disabled(context);
            BingWallpaperAlarmManager.disabled(context);
            if (force || Settings.getJobType(context) == Settings.LIVE_WALLPAPER) {
                try {
                    WallpaperManager.getInstance(context).clear();
                } catch (Exception ignored) {
                }
            }
        }).start();
        clear(context);
    }

    public static void clear(Context context) {
        Settings.setJobType(context, Settings.NONE);
        Settings.setLastWallpaperImageUrl(context, "");
        new Thread(() -> {
            BingWallpaperUtils.clearTaskComplete(context);
        }).start();
    }

    public static int enabled(Context context) {
        int ret = enabledJob(context);
        if (ret == Settings.NONE) {
            Toast.makeText(context, R.string.enable_job_error, Toast.LENGTH_LONG).show();
        }
        return ret;
    }

    @Settings.JobType
    public static int enabledJob(Context context) {
        try {
            clear(context);
            int type = Settings.getAutomaticUpdateType(context);
            if (type == Settings.AUTOMATIC_UPDATE_TYPE_AUTO) {
                if (BingWallpaperUtils.isROMSystem()) {
                    if (enableLiveService(context)) {
                        return Settings.LIVE_WALLPAPER;
                    }
                    if (enableSystem(context)) {
                        return Settings.WORKER;
                    }
                } else {
                    if (enableSystem(context)) {
                        return Settings.WORKER;
                    }
                    if (enableLiveService(context)) {
                        return Settings.LIVE_WALLPAPER;
                    }
                }
                if (enableTimer(context)) {
                    return Settings.TIMER;
                }
            } else if (type == Settings.AUTOMATIC_UPDATE_TYPE_SYSTEM) {
                if (enableSystem(context)) {
                    return Settings.WORKER;
                }
            } else if (type == Settings.AUTOMATIC_UPDATE_TYPE_SERVICE) {
                if (enableLiveService(context)) {
                    return Settings.LIVE_WALLPAPER;
                }
            } else if (type == Settings.AUTOMATIC_UPDATE_TYPE_TIMER) {
                if (enableTimer(context)) {
                    return Settings.TIMER;
                }
            }
        } catch (Throwable ignore) {
        }
        return Settings.NONE;
    }

    public static boolean enableSystem(Context context) {
        long time = TimeUnit.HOURS.toSeconds(Settings.getAutomaticUpdateInterval(context));
        boolean enabled = WorkerManager.enabled(context, time);
        if (enabled) {
            Settings.setJobType(context, Settings.WORKER);
            new Thread(() -> {
                if (Settings.isEnableLog(context)) {
                    LogDebugFileUtils.get().i(TAG, "Enable scheduler interval time : %s", time);
                }
            }).start();
            L.alog().d(TAG, "enable scheduler interval time : %s", time);
        }
        return enabled;
    }

    public static boolean enableTimer(Context context) {
        LocalTime updateTime = BingWallpaperUtils.getDayUpdateTime(context);
        boolean enabled = BingWallpaperAlarmManager.enabled(context, updateTime);
        if (enabled) {
            Settings.setJobType(context, Settings.TIMER);
            new Thread(() -> {
                if (Settings.isEnableLog(context)) {
                    LogDebugFileUtils.get().i(TAG, "Enable timer time : %s", updateTime.toString("HH:mm"));
                }
            }).start();
            L.alog().d(TAG, "enable timer time : %s", updateTime.toString("HH:mm"));
        }
        return enabled;
    }

    public static boolean enableLiveService(Context context) {
        try {
            startLiveService(context);
            return true;
        } catch (Throwable ignored) {
        }
        return false;
    }

    public static int LIVE_WALLPAPER_REQUEST_CODE = 0x99;

    public static void startLiveService(Context context) {
        Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                new ComponentName(context, LiveWallpaperService.class));
        if (intent.resolveActivity(context.getPackageManager()) == null) {
            throw new android.content.ActivityNotFoundException("LiveWallpaperService");
        }
        if (context instanceof Activity) {
            new Thread(() -> ((Activity) context).startActivityForResult(intent, LIVE_WALLPAPER_REQUEST_CODE)).start();
        }
    }

    public static void onActivityResult(Context context, int requestCode, int resultCode, YNCallback callback) {
        if (requestCode == LIVE_WALLPAPER_REQUEST_CODE) {
            if (Activity.RESULT_OK == resultCode) {
                Settings.setJobType(context, Settings.LIVE_WALLPAPER);
                new Thread(() -> {
                    if (Settings.isEnableLog(context)) {
                        LogDebugFileUtils.get().i(TAG, "Enable live wallpaper");
                    }
                }).start();
                L.alog().d(TAG, "enable live wallpaper");
                if (callback != null) {
                    callback.onAllow();
                }
            } else {
                if (callback != null) {
                    callback.onDeny();
                }
            }
        }
    }

    public static String check(Context context) {
        int jobType = Settings.getJobType(context);
        if (jobType == Settings.NONE) {
            return "none";
        } else if (jobType == Settings.LIVE_WALLPAPER) {
            if (checkLiveWallpaperService()) {
                return "live_wallpaper";
            }
            return "live_wallpaper_error";
        } else if (jobType == Settings.WORKER) {
            if (WorkerManager.isScheduled(context)) {
                return "worker";
            } else {
                return "worker_error";
            }
        } else if (jobType == Settings.TIMER) {
            return "timer(" + BingWallpaperUtils.getDayUpdateTime(context) + ")";
        }
        return String.valueOf(jobType);
    }

    public static boolean checkLiveWallpaperService() {
        long heartbeat = Settings.getLiveWallpaperHeartbeat();
        return heartbeat > 0 && (System.currentTimeMillis() - heartbeat <= Constants.DEF_LIVE_WALLPAPER_CHECK_PERIODIC);
    }

}
