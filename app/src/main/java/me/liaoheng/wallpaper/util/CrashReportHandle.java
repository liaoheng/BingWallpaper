package me.liaoheng.wallpaper.util;

import android.content.Context;
import android.text.TextUtils;

import com.bumptech.glide.load.engine.GlideException;
import com.crashlytics.android.Crashlytics;
import com.github.liaoheng.common.util.L;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.net.SocketTimeoutException;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import me.liaoheng.wallpaper.BuildConfig;
import me.liaoheng.wallpaper.R;

/**
 * @author liaoheng
 * @version 2018-04-23 23:25
 */
public class CrashReportHandle {

    public static void init(Context context) {
        if (check(context)) {
            disable(context);
        } else {
            enable(context);
        }
    }

    public static void enable(Context context) {
        if (!Fabric.isInitialized()) {
            Fabric.with(context, new Crashlytics());
        }
        FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(true);
    }

    public static void disable(Context context) {
        FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(false);
    }

    public static String loadFailed(Context context, String TAG, Throwable throwable) {
        String error = context.getString(R.string.network_request_error);
        if (throwable == null) {
            L.alog().e(TAG, error);
        } else {
            if (throwable instanceof GlideException) {
                GlideException e = (GlideException) throwable;
                error = context.getString(R.string.load_image_error);
                List<Throwable> causes = e.getCauses();
                if (causes != null) {
                    for (Throwable t : causes) {
                        if (t instanceof SocketTimeoutException) {
                            error = context.getString(R.string.connection_timed_out);
                            break;
                        }
                    }
                }
            } else {
                if (throwable instanceof SocketTimeoutException) {
                    error = context.getString(R.string.connection_timed_out);
                }
            }
            L.alog().e(TAG, throwable);
            collectException(context, TAG, throwable);
        }
        return error;
    }

    public static void saveWallpaper(Context context, String TAG, Throwable t) {
        L.alog().e(TAG, t, "save wallpaper error");
        if (BingWallpaperUtils.isEnableLogProvider(context)) {
            LogDebugFileUtils.get().e(TAG, "Save wallpaper error: %s", t);
        }
        CrashReportHandle.collectException(context, TAG, t);
    }

    public static void collectException(Context context, String TAG, Throwable t) {
        collectException(context, TAG, null, t);
    }

    public static void collectException(Context context, String TAG, String msg, Throwable t) {
        if (check(context)) {
            return;
        }
        try {
            Crashlytics.log("TAG: " + TAG);
            if (!TextUtils.isEmpty(msg)) {
                Crashlytics.log(msg);
            }
            Crashlytics.log("Feedback info: " + BingWallpaperUtils.getSystemInfo(context));
            Crashlytics.logException(t);
        } catch (Exception ignored) {
        }
    }

    private static boolean check(Context context) {
        return !BingWallpaperUtils.isCrashReport(context) || BuildConfig.DEBUG;
    }
}
