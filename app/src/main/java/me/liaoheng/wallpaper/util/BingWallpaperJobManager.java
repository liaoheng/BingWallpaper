package me.liaoheng.wallpaper.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.annotation.IntDef;
import androidx.annotation.MainThread;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.github.liaoheng.common.util.L;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.TimeUnit;

import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.service.WallpaperDaemonService;

/**
 * @author liaoheng
 * @version 2017-12-21 15:25
 */
public class BingWallpaperJobManager {
    private static final String TAG = BingWallpaperJobManager.class.getSimpleName();

    public static void disabled(Context context) {
        clear(context);

        WorkerManager.disabled(context);

        Intent intent = new Intent(context, WallpaperDaemonService.class);
        context.stopService(intent);
    }

    public static void clear(Context context) {
        setJobType(context, NONE);
    }

    public static boolean enabled(Context context) {
        clear(context);
        boolean ret = enabled(context,
                TimeUnit.HOURS.toSeconds(BingWallpaperUtils.getAutomaticUpdateInterval(context)));
        if (!ret) {
            Toast.makeText(context, R.string.enable_job_error, Toast.LENGTH_LONG).show();
        }
        return ret;
    }

    /**
     * @param time seconds
     */
    public static boolean enableSystem(Context context, long time) {
        return WorkerManager.enabled(context, time);
    }

    /**
     * @param time seconds
     */
    public static boolean enabled(Context context, long time) {
        try {
            int type = BingWallpaperUtils.getAutomaticUpdateType(context);
            if (type == BingWallpaperUtils.AUTOMATIC_UPDATE_TYPE_AUTO) {
                if (!enableSystem(context, time)) {
                    return enableDaemonService(context);
                }
            } else if (type == BingWallpaperUtils.AUTOMATIC_UPDATE_TYPE_SYSTEM) {
                return enableSystem(context, time);
            } else if (type == BingWallpaperUtils.AUTOMATIC_UPDATE_TYPE_SERVICE) {
                return enableDaemonService(context);
            }
            return true;
        } catch (Exception ignore) {
            return false;
        }
    }

    public static long startDaemonService(Context context) {
        long time = Constants.DAEMON_SERVICE_PERIODIC;
        startDaemonService(context, time);
        return time;
    }

    /**
     * @param time seconds
     */
    public static void startDaemonService(Context context, long time) {
        Intent intent = new Intent(context, WallpaperDaemonService.class);
        intent.putExtra("time", time);
        ContextCompat.startForegroundService(context, intent);
    }

    public static boolean enableDaemonService(Context context) {
        try {
            long time = startDaemonService(context);
            setJobType(context, DAEMON_SERVICE);
            if (BingWallpaperUtils.isEnableLog(context)) {
                LogDebugFileUtils.get()
                        .i(TAG, "Enable daemon service interval time : %s", time);
            }
            L.alog().d(TAG, "Enable daemon service interval time : %s", time);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static void setJobType(Context context, @JobType int type) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        sharedPreferences.edit().putInt("bing_wallpaper_job_type", type).apply();
    }

    @JobType
    public static int getJobType(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sharedPreferences.getInt("bing_wallpaper_job_type", -1);
    }

    public static boolean isJobTypeDaemonService(Context context) {
        return getJobType(context) == DAEMON_SERVICE;
    }

    @IntDef(value = {
            NONE,
            GOOGLE_SERVICE,
            SYSTEM,
            DAEMON_SERVICE,
            WORKER
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface JobType {}

    public final static int NONE = -1;
    public final static int GOOGLE_SERVICE = 0;
    public final static int SYSTEM = 1;
    public final static int DAEMON_SERVICE = 2;
    public final static int WORKER = 3;

    public static String check(Context context) {
        int jobType = getJobType(context);
        if (jobType == NONE) {
            return "none";
        } else if (jobType == DAEMON_SERVICE) {
            return "daemon_service";
        } else if (jobType == WORKER) {
            if (WorkerManager.isScheduled(context)) {
                return "worker";
            } else {
                return "worker_error";
            }
        }
        return String.valueOf(jobType);
    }

    @MainThread
    public static void restore(Context context) {
        if (isJobTypeDaemonService(context)) {
            startDaemonService(context);
        }
    }

}
