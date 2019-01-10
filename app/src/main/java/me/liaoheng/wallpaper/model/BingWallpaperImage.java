package me.liaoheng.wallpaper.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;

/**
 * @author liaoheng
 * @version 2016-09-19 11:23
 */
public class BingWallpaperImage implements Parcelable {
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
    @Expose(serialize = false, deserialize = false)
    private String imageUrl;

    public BingWallpaperImage copy(String imageUrl) {
        return new BingWallpaperImage(startdate, fullstartdate, enddate, url, urlbase, copyright, copyrightlink, wp,
                hsh, drk, top, bot, caption, copyrightonly, desc, imageUrl);
    }

    public BingWallpaperImage(String copyright) {
        this.copyright = copyright;
    }

    public BingWallpaperImage(String startdate, String fullstartdate, String enddate, String url, String urlbase,
            String copyright, String copyrightlink, boolean wp, String hsh, int drk, int top, int bot,
            String caption, String copyrightonly, String desc, String imageUrl) {
        this.startdate = startdate;
        this.fullstartdate = fullstartdate;
        this.enddate = enddate;
        this.url = url;
        this.urlbase = urlbase;
        this.copyright = copyright;
        this.copyrightlink = copyrightlink;
        this.wp = wp;
        this.hsh = hsh;
        this.drk = drk;
        this.top = top;
        this.bot = bot;
        this.caption = caption;
        this.copyrightonly = copyrightonly;
        this.desc = desc;
        this.imageUrl = imageUrl;
    }

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

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getCopyrightonly() {
        return copyrightonly;
    }

    public void setCopyrightonly(String copyrightonly) {
        this.copyrightonly = copyrightonly;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
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
                ", caption='" + caption + '\'' +
                ", copyrightonly='" + copyrightonly + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }

    protected BingWallpaperImage(Parcel in) {
        startdate = in.readString();
        fullstartdate = in.readString();
        enddate = in.readString();
        url = in.readString();
        urlbase = in.readString();
        copyright = in.readString();
        copyrightlink = in.readString();
        wp = in.readByte() != 0;
        hsh = in.readString();
        drk = in.readInt();
        top = in.readInt();
        bot = in.readInt();
        caption = in.readString();
        copyrightonly = in.readString();
        desc = in.readString();
        imageUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(startdate);
        dest.writeString(fullstartdate);
        dest.writeString(enddate);
        dest.writeString(url);
        dest.writeString(urlbase);
        dest.writeString(copyright);
        dest.writeString(copyrightlink);
        dest.writeByte((byte) (wp ? 1 : 0));
        dest.writeString(hsh);
        dest.writeInt(drk);
        dest.writeInt(top);
        dest.writeInt(bot);
        dest.writeString(caption);
        dest.writeString(copyrightonly);
        dest.writeString(desc);
        dest.writeString(imageUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BingWallpaperImage> CREATOR = new Creator<BingWallpaperImage>() {
        @Override
        public BingWallpaperImage createFromParcel(Parcel in) {
            return new BingWallpaperImage(in);
        }

        @Override
        public BingWallpaperImage[] newArray(int size) {
            return new BingWallpaperImage[size];
        }
    };
}
