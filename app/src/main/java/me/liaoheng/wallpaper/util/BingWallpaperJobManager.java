package me.liaoheng.wallpaper.util;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.IntDef;

import com.github.liaoheng.common.util.Callback5;
import com.github.liaoheng.common.util.L;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
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
        WorkerManager.disabled(context);
        if (getJobType(context) == LIVE_WALLPAPER) {
            try {
                WallpaperManager.getInstance(context).clear();
            } catch (IOException ignored) {
            }
        }
        clear(context);
    }

    public static void clear(Context context) {
        BingWallpaperUtils.clearTaskComplete(context);
        SettingUtils.setLastWallpaperImageUrl(context, "");
        setJobType(context, NONE);
    }

    public static boolean enabled(Context context) {
        clear(context);
        boolean ret = enabled(context,
                TimeUnit.HOURS.toSeconds(SettingUtils.getAutomaticUpdateInterval(context)));
        if (!ret) {
            Toast.makeText(context, R.string.enable_job_error, Toast.LENGTH_LONG).show();
        }
        return ret;
    }

    /**
     * @param time seconds
     */
    public static boolean enableSystem(Context context, long time) {
        try {
            boolean enabled = WorkerManager.enabled(context, time);
            setJobType(context, WORKER);
            if (BingWallpaperUtils.isEnableLog(context)) {
                LogDebugFileUtils.get()
                        .i(TAG, "Enable worker interval time : %s", time);
            }
            L.alog().d(TAG, "enable worker interval time : %s", time);
            return enabled;
        } catch (Throwable e) {
            return false;
        }
    }

    /**
     * @param time seconds
     */
    public static boolean enabled(Context context, long time) {
        try {
            int type = SettingUtils.getAutomaticUpdateType(context);
            if (type == SettingUtils.AUTOMATIC_UPDATE_TYPE_AUTO) {
                if (!enableSystem(context, time)) {
                    return enableLiveService(context, time);
                }
            } else if (type == SettingUtils.AUTOMATIC_UPDATE_TYPE_SYSTEM) {
                return enableSystem(context, time);
            } else if (type == SettingUtils.AUTOMATIC_UPDATE_TYPE_SERVICE) {
                return enableLiveService(context, time);
            }
            return true;
        } catch (Exception ignore) {
            return false;
        }
    }

    public static int LIVE_WALLPAPER_REQUEST_CODE = 0x99;

    public static void startLiveService(Context context) {
        Intent intent = new Intent(
                WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                new ComponentName(context, LiveWallpaperService.class));
        if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(intent, LIVE_WALLPAPER_REQUEST_CODE);
        }
    }

    public static boolean enableLiveService(Context context, long time) {
        try {
            startLiveService(context);
            //setJobType(context, LIVE_WALLPAPER);
            //if (BingWallpaperUtils.isEnableLog(context)) {
            //    LogDebugFileUtils.get()
            //            .i(TAG, "Enable live service");
            //}
            //L.alog().d(TAG, "enable live service");
        } catch (Throwable e) {
            return false;
        }
        return true;
    }

    public static void onActivityResult(Context context, int requestCode, int resultCode) {
        onActivityResult(context, requestCode, resultCode, null);
    }

    public static void onActivityResult(Context context, int requestCode, int resultCode, Callback5 callback) {
        if (requestCode == LIVE_WALLPAPER_REQUEST_CODE) {
            if (Activity.RESULT_OK == resultCode) {
                Intent intent = new Intent(LiveWallpaperService.START_LIVE_WALLPAPER_SCHEDULER);
                context.sendBroadcast(intent);
                setJobType(context, LIVE_WALLPAPER);
                if (BingWallpaperUtils.isEnableLog(context)) {
                    LogDebugFileUtils.get()
                            .i(TAG, "Enable live service");
                }
                L.alog().d(TAG, "enable live service");
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

    public static void setJobType(Context context, @JobType int type) {
        SettingTrayPreferences.get(context).put("bing_wallpaper_job_type", type);
    }

    @JobType
    public static int getJobType(Context context) {
        return SettingTrayPreferences.get(context).getInt("bing_wallpaper_job_type");
    }

    @IntDef(value = {
            NONE,
            GOOGLE_SERVICE,
            SYSTEM,
            DAEMON_SERVICE,
            WORKER,
            LIVE_WALLPAPER
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface JobType {}

    public final static int NONE = -1;
    public final static int GOOGLE_SERVICE = 0;
    public final static int SYSTEM = 1;
    public final static int DAEMON_SERVICE = 2;
    public final static int WORKER = 3;
    public final static int LIVE_WALLPAPER = 4;

    public static String check(Context context) {
        int jobType = getJobType(context);
        if (jobType == NONE) {
            return "none";
        } else if (jobType == LIVE_WALLPAPER) {
            return "live_wallpaper";
        } else if (jobType == WORKER) {
            if (WorkerManager.isScheduled(context)) {
                return "worker";
            } else {
                return "worker_error";
            }
        }
        return String.valueOf(jobType);
    }

}
