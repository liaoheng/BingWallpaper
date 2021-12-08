package me.liaoheng.wallpaper.util;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author liaoheng
 * @date 2021-12-08 22:27
 */
public class HandlerHelper implements HandlerCallback {

    private HandlerThread mHandlerThread;
    private DelayedHandler mHandler;

    public HandlerHelper(String name, int priority, Handler.Callback callback) {
        mHandlerThread = new HandlerThread(name, priority);
        mHandlerThread.start();
        mHandler = new DelayedHandler(mHandlerThread.getLooper(), callback);
    }

    public static HandlerHelper create(String name, int priority, Handler.Callback callback) {
        return new HandlerHelper(name, priority, callback);
    }

    public void release() {
        if (mHandlerThread != null) {
            mHandlerThread.quitSafely();
            mHandlerThread = null;
        }
        mHandler = null;
    }

    public final void removeCallbacks(Runnable r) {
        mHandler.removeCallbacks(r);
    }

    public final boolean post(Runnable r) {
        return mHandler.post(r);
    }

    public final boolean postDelayed(Runnable r, long delayMillis) {
        return mHandler.postDelayed(r, delayMillis);
    }

    public final void removeMessages(int what) {
        mHandler.removeMessages(what);
    }

    @Override
    public void sendDelayed(int what, Object obj, long delayMillis) {
        mHandler.sendDelayed(what, obj, delayMillis);
    }

    @Override
    public void sendDelayed(int what, long delayMillis) {
        mHandler.sendDelayed(what, delayMillis);
    }

}
