package me.liaoheng.wallpaper.util;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.TimeUnit;

/**
 * @author liaoheng
 * @version 2016-09-20 11:28
 */
public interface Constants {

    String PROJECT_NAME = "BingWallpaper";

    String DISK_CACHE_DIR = "imgCache";
    String HTTP_CACHE_DIR = "httpCache";
    int IMAGE_DISK_CACHE_SIZE = 100 * 1024 * 1024; // 100MB
    int HTTP_DISK_CACHE_SIZE = 5 * 1024 * 1024;  // 5MB

    long JOB_SCHEDULER_PERIODIC = TimeUnit.HOURS.toMillis(3);

    String BASE_URL = "https://www.bing.com";
    String CHINA_BASE_URL = "https://www.bing.com";
    String GLOBAL_BASE_URL = "https://global.bing.com";

    String CHINA_URL = "https://www.bing.com/HPImageArchive.aspx?format=js&idx=%s&n=%s";
    String GLOBAL_URL = "https://global.bing.com/HPImageArchive.aspx?format=js&idx=%s&n=%s&pid=hp";

    String MKT_HEADER = "_EDGE_S=mkt=%s";

    class WallpaperConfig {
        public static int WALLPAPER_RESOLUTION_WIDTH = 480;
        public static int WALLPAPER_RESOLUTION_HEIGHT = 800;
        public static String WALLPAPER_RESOLUTION = WALLPAPER_RESOLUTION_WIDTH + "x" + WALLPAPER_RESOLUTION_HEIGHT;
        public static String MAIN_WALLPAPER_RESOLUTION = "720x1280";
    }

    class Config {
        public static boolean isPhone = true;
    }

    /**
     * 0. both , 1. home , 2. lock
     */
    @IntDef(value = {
            EXTRA_SET_WALLPAPER_MODE_BOTH,
            EXTRA_SET_WALLPAPER_MODE_HOME,
            EXTRA_SET_WALLPAPER_MODE_LOCK,
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface setWallpaperMode {}

    int EXTRA_SET_WALLPAPER_MODE_BOTH = 0;
    int EXTRA_SET_WALLPAPER_MODE_HOME = 1;
    int EXTRA_SET_WALLPAPER_MODE_LOCK = 1 << 1;

    String FOREGROUND_SET_NOTIFICATION_CHANNEL = "bing_wallpaper_intent_service_notification_channel_id";

    String ACTION_UPDATE_WALLPAPER_COVER_STORY = "me.liaoheng.wallpaper.UPDATE_WALLPAPER_COVER_STORY";
    String EXTRA_UPDATE_WALLPAPER_COVER_STORY = "WALLPAPER_COVER_STORY";
}
