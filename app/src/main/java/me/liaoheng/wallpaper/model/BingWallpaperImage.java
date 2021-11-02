package me.liaoheng.wallpaper.model;

/**
 * @author liaoheng
 * @version 2016-09-19 11:17
 */
public class BingWallpaperImage {
    private String startdate;
    private String fullstartdate;
    private String enddate;
    private String url;
    private String urlbase;
    private String copyright;
    private String copyrightlink;
    private boolean wp;
    private String hsh;
    private int drk;
    private int top;
    private int bot;
    private String caption;
    private String copyrightonly;
    private String desc;

    public Wallpaper to(BingWallpaper.ToolTips toolTips) {
        return new Wallpaper(enddate, url, urlbase, copyright, copyrightlink, desc, toolTips == null ? "" : wp ? toolTips.getWalls() : toolTips.getWalle());
    }
}
