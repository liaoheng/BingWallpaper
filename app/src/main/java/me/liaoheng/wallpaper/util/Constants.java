package me.liaoheng.wallpaper.util;

/**
 * @author liaoheng
 * @version 2016-09-20 11:28
 */
public interface Constants {

    String PROJECT_NAME          = "BingWallpaper";

    String DISK_CACHE_DIR        = "imgCache";
    String HTTP_CACHE_DIR        = "httpCache";
    int    IMAGE_DISK_CACHE_SIZE = 100 * 1024 * 1024; // 100MB
    int    HTTP_DISK_CACHE_SIZE  = 5 * 1024 * 1024;  // 5MB

    String BASE_URL = "https://www.bing.com";

    String CHINA_URL  = "https://cn.bing.com/HPImageArchive.aspx?format=js&idx=%s&n=%s";
    String GLOBAL_URL = "https://global.bing.com/HPImageArchive.aspx?format=js&idx=%s&n=%s";

}
