package me.liaoheng.bingwallpaper.util;

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

    String BASE_URL = "http://www.bing.com";

    String CHINA_URL  = "http://www.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1";
    String GLOBAL_URL = "http://global.bing.com/HPImageArchive.aspx?setmkt=en-us&format=js&idx=0&n=1";

}
