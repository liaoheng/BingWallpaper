package me.liaoheng.wallpaper.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.Constants;

/**
 * @author liaoheng
 * @version 2019-07-31 17:00
 */
public class Config implements Parcelable {
    private int stackBlur;
    @Constants.setWallpaperMode
    private int stackBlurMode;
    private boolean showNotification;
    private boolean background;
    /**
     * 0. both , 1. home , 2. lock
     */
    @Constants.setWallpaperMode
    private int wallpaperMode;

    public static class Builder {
        private int stackBlur;
        @Constants.setWallpaperMode
        private int stackBlurMode = Constants.EXTRA_SET_WALLPAPER_MODE_BOTH;
        private boolean showNotification;
        private boolean background;
        @Constants.setWallpaperMode
        private int wallpaperMode = Constants.EXTRA_SET_WALLPAPER_MODE_BOTH;

        public Builder loadConfig(Context context) {
            this.stackBlur = BingWallpaperUtils.getSettingStackBlur(context);
            this.stackBlurMode = BingWallpaperUtils.getSettingStackBlurMode(context);
            return this;
        }

        public Builder setShowNotification(boolean showNotification) {
            this.showNotification = showNotification;
            return this;
        }

        public Builder setBackground(boolean background) {
            this.background = background;
            return this;
        }

        public Builder setWallpaperMode(int wallpaperMode) {
            this.wallpaperMode = wallpaperMode;
            return this;
        }

        public Config build() {
            return new Config(stackBlur, stackBlurMode, showNotification, background, wallpaperMode);
        }
    }

    private Config() {
    }

    private Config(int stackBlur, int stackBlurMode, boolean showNotification, boolean background, int wallpaperMode) {
        this.stackBlur = stackBlur;
        this.stackBlurMode = stackBlurMode;
        this.showNotification = showNotification;
        this.background = background;
        this.wallpaperMode = wallpaperMode;
    }

    public int getStackBlur() {
        return stackBlur;
    }

    @Constants.setWallpaperMode
    public int getStackBlurMode() {
        return stackBlurMode;
    }

    public void setStackBlur(@Constants.setWallpaperMode int stackBlur) {
        this.stackBlur = stackBlur;
    }

    public boolean isShowNotification() {
        return showNotification;
    }

    public boolean isBackground() {
        return background;
    }

    @Constants.setWallpaperMode
    public int getWallpaperMode() {
        return wallpaperMode;
    }

    public void setWallpaperMode(@Constants.setWallpaperMode int wallpaperMode) {
        this.wallpaperMode = wallpaperMode;
    }

    @Override
    public String toString() {
        return "Config{" +
                "stackBlur=" + stackBlur +
                ", stackBlurMode=" + stackBlurMode +
                ", showNotification=" + showNotification +
                ", background=" + background +
                ", wallpaperMode=" + wallpaperMode +
                '}';
    }

    protected Config(Parcel in) {
        stackBlur = in.readInt();
        stackBlurMode = in.readInt();
        showNotification = in.readInt() == 1;
        background = in.readInt() == 1;
        wallpaperMode = in.readInt();
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
        dest.writeInt(stackBlurMode);
        dest.writeInt(showNotification ? 1 : 0);
        dest.writeInt(background ? 1 : 0);
        dest.writeInt(wallpaperMode);
    }
}
