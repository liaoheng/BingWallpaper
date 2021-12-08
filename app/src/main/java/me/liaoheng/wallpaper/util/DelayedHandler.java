package me.liaoheng.wallpaper.util;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author liaoheng
 * @date 2021-12-08 23:54
 */
public class DelayedHandler extends Handler implements HandlerCallback {

    public DelayedHandler(@NonNull Looper looper, @Nullable Callback callback) {
        super(looper, callback);
    }

    @Override
    public void sendDelayed(int what, Object obj, long delayMillis) {
        sendMessageDelayed(obtainMessage(what, obj), delayMillis);
    }

    @Override
    public void sendDelayed(int what, long delayMillis) {
        sendDelayed(what, null, delayMillis);
    }
}
