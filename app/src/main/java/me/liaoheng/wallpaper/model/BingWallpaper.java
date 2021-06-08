package me.liaoheng.wallpaper.model;


import java.util.List;

/**
 * @author liaoheng
 * @version 2016-09-19 11:17
 */
public class BingWallpaper {

    private List<BingWallpaperImage> images;
    private ToolTips tooltips;

    public List<BingWallpaperImage> getImages() {
        return images;
    }

    public void setImages(List<BingWallpaperImage> images) {
        this.images = images;
    }

    public ToolTips getTooltips() {
        return tooltips;
    }

    public void setTooltips(ToolTips tooltips) {
        this.tooltips = tooltips;
    }

    public class ToolTips {
        private String walle;
        private String walls;

        public String getWalle() {
            return walle;
        }

        public void setWalle(String walle) {
            this.walle = walle;
        }

        public String getWalls() {
            return walls;
        }

        public void setWalls(String walls) {
            this.walls = walls;
        }
    }
}
