package me.liaoheng.wallpaper.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.NotNull;

import me.liaoheng.wallpaper.util.BingWallpaperUtils;

/**
 * @author liaoheng
 * @version 2019-07-31 17:00
 */
public class Config implements Parcelable {
    private int stackBlur;

    public int getStackBlur() {
        return stackBlur;
    }

    public void setStackBlur(int stackBlur) {
        this.stackBlur = stackBlur;
    }

    public Config(Context context) {
        this.stackBlur = BingWallpaperUtils.getSettingStackBlur(context);
    }

    public Config() {

    }

    @NotNull
    @Override
    public String toString() {
        return "Config{" +
                "stackBlur=" + stackBlur +
                '}';
    }

    protected Config(Parcel in) {
        stackBlur = in.readInt();
    }

    public static final Creator<Config> CREATOR = new Creator<Config>() {
        @Override
        public Config createFromParcel(Parcel in) {
            return new Config(in);
        }

        @Override
        public Config[] newArray(int size) {
            return new Config[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(stackBlur);
    }
}
