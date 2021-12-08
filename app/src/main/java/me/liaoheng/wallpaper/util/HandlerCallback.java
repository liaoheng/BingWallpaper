package me.liaoheng.wallpaper.util;

/**
 * @author liaoheng
 * @date 2021-12-08 22:36
 */
public interface HandlerCallback {
    void sendDelayed(int what, Object obj, long delayMillis);

    void sendDelayed(int what, long delayMillis);
}
