package me.liaoheng.wallpaper.util;

import java.io.IOException;

/**
 * @author liaoheng
 * @version 2020-09-09 17:11
 */
public class LockSetWallpaperException extends IOException {
    public LockSetWallpaperException() {
    }

    public LockSetWallpaperException(String message) {
        super(message);
    }

    public LockSetWallpaperException(String message, Throwable cause) {
        super(message, cause);
    }

    public LockSetWallpaperException(Throwable cause) {
        super(cause);
    }
}
