package me.liaoheng.wallpaper.model;

import java.io.File;

/**
 * @author liaoheng
 * @date 2022-01-15 17:21
 */
public class WallpaperImage {
    private String url;
    private File home;
    private File lock;

    public WallpaperImage(String url, File home, File lock) {
        this.url = url;
        this.home = home;
        this.lock = lock;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public File getHome() {
        return home;
    }

    public void setHome(File home) {
        this.home = home;
    }

    public File getLock() {
        return lock;
    }

    public void setLock(File lock) {
        this.lock = lock;
    }
}
