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
    private final String title;
    private final String desc;
    private final String webUrl;
    private final String url;
    private final String baseUrl;
    private final String dateTime;
    private final String copyrightInfo;
    private String imageUrl;

    public Wallpaper copy(String imageUrl) {
        Wallpaper wallpaper = new Wallpaper(dateTime, url, baseUrl, title, webUrl, desc, copyrightInfo);
        wallpaper.setImageUrl(imageUrl);
        return wallpaper;
    }

    public Wallpaper(String dateTime, String url, String baseUrl, String title,
            String webUrl, String desc, String copyrightInfo) {
        this.dateTime = dateTime;
        this.url = url;
        this.baseUrl = baseUrl;
        this.title = title;
        this.webUrl = webUrl;
        this.desc = desc;
        this.copyrightInfo = copyrightInfo;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getUrl() {
        return url;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public String getDesc() {
        return desc;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setResolutionImageUrl(Context context) {
        this.imageUrl = BingWallpaperUtils.getResolutionImageUrl(context, baseUrl);
    }

    public String getCopyrightInfo() {
        return copyrightInfo;
    }

    @NonNull
    @Override
    public String toString() {
        return "Wallpaper{" +
                "title='" + title + '\'' +
                ", desc='" + desc + '\'' +
                ", webUrl='" + webUrl + '\'' +
                ", url='" + url + '\'' +
                ", baseUrl='" + baseUrl + '\'' +
                ", dateTime='" + dateTime + '\'' +
                ", copyrightInfo='" + copyrightInfo + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }

    protected Wallpaper(Parcel in) {
        dateTime = in.readString();
        url = in.readString();
        baseUrl = in.readString();
        title = in.readString();
        desc = in.readString();
        webUrl = in.readString();
        imageUrl = in.readString();
        copyrightInfo = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(dateTime);
        dest.writeString(url);
        dest.writeString(baseUrl);
        dest.writeString(title);
        dest.writeString(desc);
        dest.writeString(webUrl);
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
