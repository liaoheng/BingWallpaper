package me.liaoheng.wallpaper.model;

import java.io.Serializable;

/**
 * @author liaoheng
 * @version 2016-09-19 11:23
 */
public class BingWallpaperImage implements
                                                                            Serializable {
    private String  startdate;
    private String  fullstartdate;
    private String  enddate;
    private String  url;
    private String  urlbase;
    private String  copyright;
    private String  copyrightlink;
    private boolean wp;
    private String  hsh;
    private int     drk;
    private int     top;
    private int     bot;

    public String getStartdate() {
        return startdate;
    }

    public void setStartdate(String startdate) {
        this.startdate = startdate;
    }

    public String getFullstartdate() {
        return fullstartdate;
    }

    public void setFullstartdate(String fullstartdate) {
        this.fullstartdate = fullstartdate;
    }

    public String getEnddate() {
        return enddate;
    }

    public void setEnddate(String enddate) {
        this.enddate = enddate;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrlbase() {
        return urlbase;
    }

    public void setUrlbase(String urlbase) {
        this.urlbase = urlbase;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public String getCopyrightlink() {
        return copyrightlink;
    }

    public void setCopyrightlink(String copyrightlink) {
        this.copyrightlink = copyrightlink;
    }

    public boolean isWp() {
        return wp;
    }

    public void setWp(boolean wp) {
        this.wp = wp;
    }

    public String getHsh() {
        return hsh;
    }

    public void setHsh(String hsh) {
        this.hsh = hsh;
    }

    public int getDrk() {
        return drk;
    }

    public void setDrk(int drk) {
        this.drk = drk;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getBot() {
        return bot;
    }

    public void setBot(int bot) {
        this.bot = bot;
    }

    @Override public String toString() {
        return "BingWallpaperImage{" +
               "startdate='" + startdate + '\'' +
               ", fullstartdate='" + fullstartdate + '\'' +
               ", enddate='" + enddate + '\'' +
               ", url='" + url + '\'' +
               ", urlbase='" + urlbase + '\'' +
               ", copyright='" + copyright + '\'' +
               ", copyrightlink='" + copyrightlink + '\'' +
               ", wp=" + wp +
               ", hsh='" + hsh + '\'' +
               ", drk=" + drk +
               ", top=" + top +
               ", bot=" + bot +
               '}';
    }
}
