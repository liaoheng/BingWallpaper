package me.liaoheng.bingwallpaper.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * @author liaoheng
 * @version 2016-09-19 11:17
 */
@JsonIgnoreProperties(ignoreUnknown = true) public class BingWallpaper {

    private List<BingWallpaperImage> images;

    public List<BingWallpaperImage> getImages() {
        return images;
    }

    public void setImages(List<BingWallpaperImage> images) {
        this.images = images;
    }
}
