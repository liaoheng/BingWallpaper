package me.liaoheng.wallpaper.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import me.liaoheng.wallpaper.util.BingWallpaperUtils;

/**
 * @author liaoheng
 * @version 2020-06-23 14:26
 */
public class Wallpaper implements Parcelable {
    private String dateTime;
    private String url;
    private String baseUrl;
    private String copyright;
    private String webUrl;
    private String desc;
    private String imageUrl;
    private String copyrightInfo;

    public Wallpaper copy(String imageUrl) {
        Wallpaper wallpaper = new Wallpaper(dateTime, url, baseUrl, copyright, webUrl, desc, copyrightInfo);
        wallpaper.setImageUrl(imageUrl);
        return wallpaper;
    }

    public Wallpaper(String dateTime, String url, String baseUrl, String copyright,
                     String webUrl, String desc, String copyrightInfo) {
        this.dateTime = dateTime;
        this.url = url;
        this.baseUrl = baseUrl;
        this.copyright = copyright;
        this.webUrl = webUrl;
        this.desc = desc;
        this.copyrightInfo = copyrightInfo;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
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

    public void setResolutionImageUrl(Context context) {
        this.imageUrl = BingWallpaperUtils.getResolutionImageUrl(context,
                this);
    }

    public String getCopyrightInfo() {
        return copyrightInfo;
    }

    public void setCopyrightInfo(String copyrightInfo) {
        this.copyrightInfo = copyrightInfo;
    }

    @NonNull
    @Override
    public String toString() {
        return "Wallpaper{" +
                "dateTime='" + dateTime + '\'' +
                ", url='" + url + '\'' +
                ", baseUrl='" + baseUrl + '\'' +
                ", copyright='" + copyright + '\'' +
                ", desc='" + desc + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }

    protected Wallpaper(Parcel in) {
        dateTime = in.readString();
        url = in.readString();
        baseUrl = in.readString();
        copyright = in.readString();
        desc = in.readString();
        imageUrl = in.readString();
        copyrightInfo = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(dateTime);
        dest.writeString(url);
        dest.writeString(baseUrl);
        dest.writeString(copyright);
        dest.writeString(desc);
        dest.writeString(imageUrl);
        dest.writeString(copyrightInfo);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Wallpaper> CREATOR = new Creator<Wallpaper>() {
        @Override
        public Wallpaper createFromParcel(Parcel in) {
            return new Wallpaper(in);
        }

        @Override
        public Wallpaper[] newArray(int size) {
            return new Wallpaper[size];
        }
    };
}
