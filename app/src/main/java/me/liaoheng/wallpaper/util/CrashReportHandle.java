package me.liaoheng.wallpaper.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.MalformedJsonException;

import com.bumptech.glide.load.engine.GlideException;
import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.ROM;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;

import javax.net.ssl.SSLHandshakeException;

import io.sentry.Sentry;
import io.sentry.android.core.SentryAndroid;
import me.liaoheng.wallpaper.BuildConfig;
import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.model.Config;

/**
 * @author liaoheng
 * @version 2018-04-23 23:25
 */
@SuppressLint("MissingPermission")
public class CrashReportHandle {
    private static boolean isFirebaseAnalytics;

    public static void init(Context context) {
        initFirebaseAnalytics();
        if (check(context)) {
            disable(context);
        } else {
            enable(context);
            SentryAndroid.init(context, options -> {
                options.setBeforeSend((event, hint) -> {
                    event.setExtra("job_ype", BingWallpaperJobManager.check(context));
                    event.setExtra("rom_type", ROM.getROM().getRom());
                    event.setExtra("resolution", Settings.getResolution(context));
                    return event;
                });
                options.setDebug(BuildConfig.DEBUG);
            });
        }
    }

    private static void initFirebaseAnalytics() {
        try {
            Class.forName("com.google.firebase.analytics.FirebaseAnalytics");
            isFirebaseAnalytics = true;
        } catch (ClassNotFoundException ignored) {
            isFirebaseAnalytics = false;
        }
    }

    public static void enable(Context context) {
        try {
            if (isFirebaseAnalytics) {
                FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(true);
            }
        } catch (Throwable ignored) {
        }
    }

    public static void disable(Context context) {
        try {
            if (isFirebaseAnalytics) {
                FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(false);
            }
        } catch (Throwable ignored) {
        }
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
        }
        return error;
    }

    public static void collectException(Context context, String TAG, Throwable t) {
        collectException(context, TAG, null, t);
    }

    public static void collectException(Context context, String TAG, Config config, Throwable t) {
        if (check(context)) {
            return;
        }
        if (t instanceof LockSetWallpaperException) {
            return;
        }
        if (t instanceof SSLHandshakeException) {
            return;
        }
        if (t instanceof MalformedJsonException) {
            return;
        }
        if (t instanceof ConnectException) {
            return;
        }
        if (t instanceof SocketException) {
            return;
        }
        if (t instanceof SocketTimeoutException) {
            return;
        }
        if (t instanceof UnknownHostException) {
            return;
        }
        try {
            Sentry.setTag("tag", TAG);
            Sentry.setExtra("Feedback info", BingWallpaperUtils.getSystemInfo(context));
            if (config != null) {
                Sentry.setExtra("Config", config.toString());
            }
            Sentry.captureException(t);
        } catch (Exception ignored) {
        }
    }

    private static boolean check(Context context) {
        return !Settings.isCrashReport(context) || BuildConfig.DEBUG;
    }
}
