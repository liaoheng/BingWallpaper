package me.liaoheng.wallpaper.util;

import android.content.Context;

import com.bumptech.glide.load.engine.GlideException;
import com.crashlytics.android.Crashlytics;
import com.github.liaoheng.common.util.L;

import java.net.SocketTimeoutException;
import java.util.List;

import me.liaoheng.wallpaper.BuildConfig;
import me.liaoheng.wallpaper.R;

/**
 * @author liaoheng
 * @version 2018-04-23 23:25
 */
public class ExceptionHandle {

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
            collectException(TAG, throwable);
        }
        return error;
    }

    public static void collectException(String TAG, Throwable t) {
        if (BuildConfig.DEBUG) {
            return;
        }
        Crashlytics.log("TAG: " + TAG);
        Crashlytics.logException(t);
    }
}
