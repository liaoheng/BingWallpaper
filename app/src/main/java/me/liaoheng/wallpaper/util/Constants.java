package me.liaoheng.wallpaper.util;

import androidx.annotation.IntDef;

import org.joda.time.LocalTime;

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
    int IMAGE_DISK_CACHE_SIZE = 50 * 1024 * 1024; // 50MB
    int HTTP_DISK_CACHE_SIZE = 5 * 1024 * 1024;  // 5MB

    long DEF_SCHEDULER_PERIODIC = 3;//hour
    String DEF_TIMER_PERIODIC = new LocalTime(0, 35).toString();
    long DEF_LIVE_WALLPAPER_CHECK_PERIODIC = TimeUnit.MINUTES.toMillis(35);
    long DEF_LIVE_WALLPAPER_CHECK_PERIODIC_EMUI = TimeUnit.MINUTES.toMillis(11);

    String LOCAL_BASE_URL = "https://www.bing.com";
    String GLOBAL_BASE_URL = "https://global.bing.com";

    String BASE_API_URL = "/HPImageArchive.aspx?format=js&idx=%s&n=%s&pid=hp&mtk=%s";
    String LOCAL_API_URL = LOCAL_BASE_URL + BASE_API_URL;
    String GLOBAL_API_URL = GLOBAL_BASE_URL + BASE_API_URL;

    String MKT_HEADER = "_EDGE_S=mkt=%s";

    String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:61.0) Gecko/20100101 Firefox/61.0";

    String DOH_CHINA = "https://dns.alidns.com/dns-query";
    String DOH_CLOUDFLARE = "https://cloudflare-dns.com/dns-query";

    class WallpaperConfig {
        public static int WALLPAPER_RESOLUTION_WIDTH = 480;
        public static int WALLPAPER_RESOLUTION_HEIGHT = 800;
        public static String WALLPAPER_RESOLUTION = WALLPAPER_RESOLUTION_WIDTH + "x" + WALLPAPER_RESOLUTION_HEIGHT;
        public static String MAIN_WALLPAPER_RESOLUTION = "720x1280";
    }

    class Config {
        public static boolean isPhone = true;
    }

    String PREF_APPWIDGET_5X1_ENABLE = "appwidget_5x1_enable";
    String PREF_APPWIDGET_5X2_ENABLE = "appwidget_5x2_enable";
    String PREF_LAST_WALLPAPER_IMAGE_URL = "last_wallpaper_image_url";

    /**
     * 0. both , 1. home , 2. lock
     */
    @IntDef(value = {
            EXTRA_SET_WALLPAPER_MODE_BOTH,
            EXTRA_SET_WALLPAPER_MODE_HOME,
            EXTRA_SET_WALLPAPER_MODE_LOCK
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface setWallpaperMode {}

    int EXTRA_SET_WALLPAPER_MODE_BOTH = 0;
    int EXTRA_SET_WALLPAPER_MODE_HOME = 1;
    int EXTRA_SET_WALLPAPER_MODE_LOCK = 1 << 1;

    String FOREGROUND_INTENT_SERVICE_NOTIFICATION_CHANNEL = "bing_wallpaper_intent_service_notification_channel_id";
    String FOREGROUND_INTENT_SERVICE_SUCCESS_NOTIFICATION_CHANNEL = "bing_wallpaper_intent_service_success_notification_channel_id";

    String ACTION_UPDATE_WALLPAPER_COVER_STORY = "me.liaoheng.wallpaper.UPDATE_WALLPAPER_COVER_STORY";
    String EXTRA_UPDATE_WALLPAPER_COVER_STORY = "WALLPAPER_COVER_STORY";

    String ACTION_GET_WALLPAPER_STATE = "me.liaoheng.wallpaper.BING_WALLPAPER_STATE";
    String EXTRA_GET_WALLPAPER_STATE = "GET_WALLPAPER_STATE";

    String TASK_FLAG_SET_WALLPAPER_STATE = "SET_WALLPAPER_STATE";

    String ACTION_DEBUG_LOG = "me.liaoheng.wallpaper.ACTION_DEBUG_LOG";
}
